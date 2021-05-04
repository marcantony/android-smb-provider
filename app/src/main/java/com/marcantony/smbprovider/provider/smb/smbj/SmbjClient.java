package com.marcantony.smbprovider.provider.smb.smbj;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.marcantony.smbprovider.provider.ServerAuthentication;
import com.marcantony.smbprovider.provider.smb.Client;
import com.marcantony.smbprovider.provider.smb.Entry;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SmbjClient implements Client {

    private static final Set<String> IGNORED_DOCUMENTS = Set.of(".", "..");
    private static final SmbConfig config = SmbConfig.builder()
            .withReadTimeout(5, TimeUnit.SECONDS)
            .withSoTimeout(10, TimeUnit.SECONDS)
            .build();

    private final SmbConnectionManager connectionManager;
    private final StorageManager storageManager;
    private final HandlerThread handlerThread;

    public SmbjClient(StorageManager storageManager) {
        SMBClient smbClient = new SMBClient(config);
        connectionManager = new SmbConnectionManager(smbClient);

        if (storageManager == null) {
            throw new NullPointerException("storage manager cannot be null");
        }
        this.storageManager = storageManager;

        handlerThread = new HandlerThread("smb");
        handlerThread.start();
    }

    @Override
    public Iterable<Entry> listDir(URI uri) {
        Path p;
        try {
            p = Paths.get(new URI("file", null, uri.getPath(), null));
        } catch (URISyntaxException e) {
            throw new RuntimeException("this shouldn't happen", e);
        }

        SmbConnectionDetails smbConnectionDetails = new SmbConnectionDetails(
                uri.getHost(),
                getAuthenticationContext(uri),
                p.getName(0).toString()
        );

        DiskShare share = connectionManager.getShare(smbConnectionDetails);
        String pathUnderShare = p.getNameCount() <= 1 ? "" : p.subpath(1, p.getNameCount()).toString();
        return share.list(pathUnderShare).stream()
                .filter(info -> !IGNORED_DOCUMENTS.contains(info.getFileName()))
                .map(info -> new SmbjEntry(info, share.folderExists(Paths.get(pathUnderShare, info.getFileName()).toString())))
                .collect(Collectors.toList());
    }

    @Override
    public ParcelFileDescriptor openProxyFile(URI uri, String mode) {
        Path p;
        try {
            p = Paths.get(new URI("file", null, uri.getPath(), null));
        } catch (URISyntaxException e) {
            throw new RuntimeException("this shouldn't happen", e);
        }

        SmbConnectionDetails smbConnectionDetails = new SmbConnectionDetails(
                uri.getHost(),
                getAuthenticationContext(uri),
                p.getName(0).toString()
        );

        Path pathUnderShare = p.getNameCount() <= 1 ? Paths.get("") : p.subpath(1, p.getNameCount());
        File file = connectionManager.getShare(smbConnectionDetails).openFile(
                pathUnderShare.toString(),
                EnumSet.of(AccessMask.FILE_READ_DATA),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null
        );
        try {
            return storageManager.openProxyFileDescriptor(
                    ParcelFileDescriptor.parseMode(mode),
                    new SmbjProxyFileDescriptorCallback(file),
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
}
