package com.marcantony.smbprovider;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.util.Log;

import androidx.annotation.Nullable;

import com.marcantony.smbprovider.smb.Client;
import com.marcantony.smbprovider.smb.EntryStats;
import com.marcantony.smbprovider.smb.SmbDetails;
import com.marcantony.smbprovider.smb.jcifs.JcifsClient;
import com.marcantony.smbprovider.smb.smbj.SmbjClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmbProvider extends DocumentsProvider {

    private static final String TAG = "SmbProvider";

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

    private SmbDetailsManager detailsManager;
    private Client smbClient;

    @Override
    public Cursor queryRoots(String[] projection) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);

        for (SmbDetails details : detailsManager.getAllDetails()) {
            String rootId = String.format("%s/%s", details.hostname, details.share);
            result.newRow()
                    .add(DocumentsContract.Root.COLUMN_ROOT_ID, rootId)
                    .add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, rootId + "/")
                    .add(DocumentsContract.Root.COLUMN_TITLE, String.format("SMB (%s)", rootId))
                    .add(DocumentsContract.Root.COLUMN_SUMMARY, details.authDetails.map(ad -> ad.username).orElse("Anonymous"))
                    .add(DocumentsContract.Root.COLUMN_FLAGS, null)
                    .add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher_foreground);
        }

        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);

        Log.d(TAG, "getting children of: " + parentDocumentId);
        smbClient.listDir(parentDocumentId).forEach(entry -> {
                    Log.d(TAG, "found child document: " + "\"" + entry.getName() + "\"");
                    String fullPath = Paths.get(parentDocumentId, entry.getName()).toString();
                    String documentId = entry.getStats().mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR) ?
                            fullPath + "/" : fullPath;
                    result.newRow()
                            .add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
                            .add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, entry.getName())
                            .add(DocumentsContract.Document.COLUMN_MIME_TYPE, entry.getStats().mimeType)
                            .add(DocumentsContract.Document.COLUMN_SIZE, entry.getStats().size)
                            .add(DocumentsContract.Document.COLUMN_FLAGS, null)
                            .add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, entry.getStats().lastModifiedMillis);
                    entry.close();
        });

        return result;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);

        Path p = Paths.get(documentId);

        Log.d(TAG, "querying document: " + "\"" + documentId + "\"");
        EntryStats stats = smbClient.stat(documentId);
        result.newRow()
                .add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
                .add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, p.getFileName().toString())
                .add(DocumentsContract.Document.COLUMN_MIME_TYPE, stats.mimeType)
                .add(DocumentsContract.Document.COLUMN_SIZE, stats.size)
                .add(DocumentsContract.Document.COLUMN_FLAGS, null)
                .add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, stats.lastModifiedMillis);

        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) {
        Log.d(TAG, "opening document: " + "\"" + documentId + "\"");
        if (!mode.equals("r")) {
            throw new UnsupportedOperationException("mode " + mode + " not supported");
        }

        return smbClient.openProxyFile(documentId, mode);
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) {
        return new AssetFileDescriptor(openDocument(documentId, "r", signal), 0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }

    @Override
    public boolean onCreate() {
        detailsManager = new SmbDetailsManager();

        StorageManager storageManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        smbClient = new JcifsClient(storageManager, executor);
        smbClient = new SmbjClient(storageManager);

        return true;
    }

}
