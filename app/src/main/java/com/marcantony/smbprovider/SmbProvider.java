package com.marcantony.smbprovider;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hierynomus.smbj.share.Share;
import com.marcantony.smbprovider.smb.SmbAuthDetails;
import com.marcantony.smbprovider.smb.SmbDetails;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbConstants;
import jcifs.SmbResource;
import jcifs.context.SingletonContext;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

public class SmbProvider extends DocumentsProvider {

    private static final String TAG = "SmbProvider";

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final Set<String> IGNORED_DOCUMENTS = Set.of(".", "..");

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[] {
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
    };
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[] {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
    };

    private StorageManager storageManager;
    private HandlerThread handlerThread;
    private SmbDetailsManager detailsManager;

    @Override
    public Cursor queryRoots(String[] projection) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);

        for (SmbDetails details : detailsManager.getAllDetails()) {
            String documentId = String.format("//%s/%s", details.hostname, details.share);
            result.newRow()
                    .add(DocumentsContract.Root.COLUMN_ROOT_ID, details.share)
                    .add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, "smb:" + documentId + "/")
                    .add(DocumentsContract.Root.COLUMN_TITLE, String.format("SMB (%s)", documentId))
                    .add(DocumentsContract.Root.COLUMN_SUMMARY, details.authDetails.map(ad -> ad.username).orElse("Anonymous"))
                    .add(DocumentsContract.Root.COLUMN_FLAGS, null)
                    .add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher_foreground);
        }

        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);

        try {
            Log.d(TAG, "getting children of: " + parentDocumentId);
            SmbFile file = new SmbFile(parentDocumentId, SingletonContext.getInstance());
            file.children().forEachRemaining(child -> {
                Log.d(TAG, "found child document: " + "\"" + child.getName() + "\"");
                String mimeType = getMimeTypeFromPath(child.getName());
                try {
                    result.newRow()
                            .add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, child.getLocator().getCanonicalURL())
                            .add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, Paths.get(child.getName()).getFileName())
                            .add(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType)
                            .add(DocumentsContract.Document.COLUMN_SIZE, child.length())
                            .add(DocumentsContract.Document.COLUMN_FLAGS, null)
                            .add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, child.lastModified());
                } catch (CIFSException e) {
                    Log.e(TAG, "could not get details of file: " + child.getName(), e);
                }
            });
        } catch (MalformedURLException | CIFSException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);

        Path p = Paths.get(documentId);

        Log.d(TAG, "querying document: " + "\"" + documentId + "\"");
        Log.d(TAG, "document has name: " + "\"" + p.getFileName() + "\"");
        String mimeType = getMimeTypeFromPath(p.getFileName().toString());
        result.newRow()
                .add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
                .add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, p.getFileName().toString())
                .add(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType)
                .add(DocumentsContract.Document.COLUMN_SIZE, null)
                .add(DocumentsContract.Document.COLUMN_FLAGS, null)
                .add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null);

        return result;
    }

    private String getMimeTypeFromPath(String path) {
        String name = Paths.get(path).getFileName().toString();
        int extensionPos = name.indexOf('.');

        if (extensionPos == -1) {
            // no "." - this is a directory
            return DocumentsContract.Document.MIME_TYPE_DIR;
        }

        String extension = name.substring(extensionPos + 1);
        String guessedMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return guessedMimeType != null ? guessedMimeType : DEFAULT_MIME_TYPE;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) {
        Log.d(TAG, "opening document: " + "\"" + documentId + "\"");
        if (!mode.equals("r")) {
            throw new UnsupportedOperationException("mode " + mode + " not supported");
        }

        try {
            return storageManager.openProxyFileDescriptor(
                    ParcelFileDescriptor.parseMode(mode),
                    new SmbProxyFileDescriptorCallback(
                            new SmbRandomAccessFile(
                                    documentId,
                                    mode,
                                    SmbConstants.DEFAULT_SHARING,
                                    SingletonContext.getInstance()
                            )
                    ),
//                    new SmbProxyFileDescriptorCallback(documentId, mode),
                    Handler.createAsync(handlerThread.getLooper())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) {
        return new AssetFileDescriptor(openDocument(documentId, "r", signal), 0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }

    @Override
    public boolean onCreate() {
        storageManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);

        handlerThread = new HandlerThread("smb");
        handlerThread.start();

        detailsManager = new SmbDetailsManager();

        return storageManager != null;
    }

}
