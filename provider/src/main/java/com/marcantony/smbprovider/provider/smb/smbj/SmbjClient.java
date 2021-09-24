package com.marcantony.smbprovider.provider.smb.smbj;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.marcantony.smbprovider.provider.smb.AsyncProxyFileDescriptorCallback;
import com.marcantony.smbprovider.provider.smb.Client;
import com.marcantony.smbprovider.provider.smb.Entry;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SmbjClient implements Client {

    private static final Set<String> IGNORED_DOCUMENTS = Set.of(".", "..");
    private static final SmbConfig config = SmbConfig.builder()
            .withReadTimeout(5, TimeUnit.SECONDS)
            .withSoTimeout(10, TimeUnit.SECONDS)
            .build();

    private final StorageManager storageManager;
    private final HandlerThread handlerThread;
    private final SMBClient smbClient;
    private final ExecutorService executor;

    public SmbjClient(StorageManager storageManager, ExecutorService executor) {
        smbClient = new SMBClient(config);

        if (storageManager == null) {
            throw new NullPointerException("storage manager cannot be null");
        }
        this.storageManager = storageManager;

        this.executor = executor;

        handlerThread = new HandlerThread("smb");
        handlerThread.start();
    }

    @Override
    public Iterable<Entry> listDir(URI uri) {
        Path p = getPath(uri);
        String pathUnderShare = p.getNameCount() <= 1 ? "" : p.subpath(1, p.getNameCount()).toString();

        try (Connection c = smbClient.connect(uri.getHost())) {
            try (Session session = c.authenticate(getAuthenticationContext(uri))) {
                try (DiskShare share = (DiskShare) session.connectShare(p.getName(0).toString())) {
                    return share.list(pathUnderShare).stream()
                            .filter(info -> !IGNORED_DOCUMENTS.contains(info.getFileName()))
                            .map(info -> new SmbjEntry(info, share.folderExists(Paths.get(pathUnderShare, info.getFileName()).toString())))
                            .collect(Collectors.toList());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ParcelFileDescriptor openProxyFile(URI uri, String mode) {
        Path p = getPath(uri);
        String pathUnderShare = p.getNameCount() <= 1 ? "" : p.subpath(1, p.getNameCount()).toString();

        try {
            return storageManager.openProxyFileDescriptor(
                    ParcelFileDescriptor.parseMode(mode),
                    new AsyncProxyFileDescriptorCallback(() ->
                            new SmbjProxyFileDescriptorCallback(
                                    smbClient,
                                    uri.getHost(),
                                    p.getName(0).toString(),
                                    pathUnderShare,
                                    getAuthenticationContext(uri)
                            ), executor
                    ),
                    Handler.createAsync(handlerThread.getLooper())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthenticationContext getAuthenticationContext(URI uri) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null) {
            return AuthenticationContext.anonymous();
        }

        String[] parts = userInfo.split(":");

        if (parts.length == 1) {
            return new AuthenticationContext(parts[0], new char[] {}, null);
        } else if (parts.length == 2) {
            return new AuthenticationContext(parts[0], parts[1].toCharArray(), null);
        } else {
            throw new RuntimeException("URI user info improperly formatted");
        }
    }

    private Path getPath(URI uri) {
        try {
            return Paths.get(new URI("file", null, uri.getPath(), null));
        } catch (URISyntaxException e) {
            throw new RuntimeException("this shouldn't happen", e);
        }

    }
}
