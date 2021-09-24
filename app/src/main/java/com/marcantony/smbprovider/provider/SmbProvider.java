package com.marcantony.smbprovider.provider;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.util.Log;

import androidx.annotation.Nullable;

import com.marcantony.smbprovider.R;
import com.marcantony.smbprovider.persistence.RoomServerInfoRepository;
import com.marcantony.smbprovider.domain.ServerInfo;
import com.marcantony.smbprovider.domain.ServerInfoRepository;
import com.marcantony.smbprovider.provider.smb.Client;
import com.marcantony.smbprovider.provider.smb.EntryStats;
import com.marcantony.smbprovider.provider.smb.jcifs.JcifsClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.schedulers.Schedulers;

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

    private List<ServerInfo> servers = Collections.emptyList();
    private Client smbClient;
    private ServerInfoRepository serverInfoRepository;

    @Override
    public Cursor queryRoots(String[] projection) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);

        for (ServerInfo info : servers) {
            String title = String.format("SMB (%s/%s)", info.host, info.share == null ? "" : info.share);
            result.newRow()
                    .add(DocumentsContract.Root.COLUMN_ROOT_ID, info.id)
                    .add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, info.id + "/")
                    .add(DocumentsContract.Root.COLUMN_TITLE, title)
                    .add(DocumentsContract.Root.COLUMN_SUMMARY, info.username != null ? info.username : "Anonymous")
                    .add(DocumentsContract.Root.COLUMN_FLAGS, null)
                    .add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher_foreground);
        }

        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);

        URI uri = documentIdToUri(parentDocumentId);

        Log.d(TAG, "getting children of: " + uri.toASCIIString());
        smbClient.listDir(uri).forEach(entry -> {
                    Log.d(TAG, "found child document: " + "\"" + entry.getName() + "\"");
                    String fullPath = Paths.get(parentDocumentId, entry.getName()).toString();
                    String documentId = entry.isDirectory() ?
                            fullPath + "/" : fullPath;
                    result.newRow()
                            .add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
                            .add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, entry.getName())
                            .add(DocumentsContract.Document.COLUMN_MIME_TYPE, entry.isDirectory() ?
                                    DocumentsContract.Document.MIME_TYPE_DIR : entry.getStats().mimeType)
                            .add(DocumentsContract.Document.COLUMN_SIZE, entry.getStats().size)
                            .add(DocumentsContract.Document.COLUMN_FLAGS, entry.getStats().mimeType.startsWith("image/") ?
                                    DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL : null)
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
        EntryStats stats = new EntryStats(documentId);
        boolean isDirectory = documentId.endsWith("/");
        result.newRow()
                .add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
                .add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, p.getFileName().toString())
                .add(DocumentsContract.Document.COLUMN_MIME_TYPE, isDirectory ?
                        DocumentsContract.Document.MIME_TYPE_DIR : stats.mimeType)
                .add(DocumentsContract.Document.COLUMN_SIZE, stats.size)
                .add(DocumentsContract.Document.COLUMN_FLAGS, null)
                .add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, stats.lastModifiedMillis);

        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) {
        URI uri = documentIdToUri(documentId);
        Log.d(TAG, "opening document: " + "\"" + uri.toASCIIString() + "\"");
        if (!mode.equals("r")) {
            throw new UnsupportedOperationException("mode " + mode + " not supported");
        }

        return smbClient.openProxyFile(uri, mode);
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) {
        return new AssetFileDescriptor(openDocument(documentId, "r", signal), 0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }

    @Override
    public boolean onCreate() {
        serverInfoRepository = RoomServerInfoRepository.getInstance(getContext());
        serverInfoRepository.getEnabledServers().subscribeOn(Schedulers.io()).subscribe(servers -> {
            this.servers = servers;
            Uri rootsUri = DocumentsContract.buildRootsUri("com.marcantony.smbprovider.documents");
            getContext().getContentResolver().notifyChange(rootsUri, null);
        });

        StorageManager storageManager = (StorageManager) getContext().getSystemService(Context.STORAGE_SERVICE);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        smbClient = new JcifsClient(storageManager, executorService);
//        smbClient = new SmbjClient(storageManager, executorService);

        return true;
    }

    private URI documentIdToUri(String documentId) {
        Path p = Paths.get(documentId);
        int serverInfoId = Integer.parseInt(p.getName(0).toString());
        ServerInfo info = serverInfoRepository.getServerInfo(serverInfoId);

        String userInfo = getUserInfo(info);
        String host = info.host;
        String path = getUriPath(info, documentId);

        try {
            return new URI("smb", userInfo, host, -1, path, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("could not build document URI", e);
        }
    }

    private String getUserInfo(ServerInfo info) {
        if (info.username == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append(info.username);

        if (info.password != null) {
            sb.append(':').append(info.password);
        }
        return sb.toString();
    }

    private String getUriPath(ServerInfo info, String documentId) {
        Path p = Paths.get(documentId);

        Path dirPath = p.getNameCount() > 1 ? p.subpath(1, p.getNameCount()) : Paths.get("");
        Path sharePath = Paths.get(info.share == null ? "" : info.share);
        String finalPath = sharePath.resolve(dirPath).toString();

        return "/" + finalPath + (documentId.endsWith("/") ? "/" : "");
    }

}
