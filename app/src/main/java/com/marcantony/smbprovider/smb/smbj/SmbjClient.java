package com.marcantony.smbprovider.smb.smbj;

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
import com.marcantony.smbprovider.smb.Client;
import com.marcantony.smbprovider.smb.Entry;

import java.io.IOException;
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
    public Iterable<Entry> listDir(String uri) {
        Path p = Paths.get(uri);
        SmbConnectionDetails smbConnectionDetails = new SmbConnectionDetails(
                p.getName(0).toString(),
                AuthenticationContext.anonymous(),
                p.getName(1).toString()
        );

        DiskShare share = connectionManager.getShare(smbConnectionDetails);
        String pathUnderShare = p.getNameCount() <= 2 ? "" : p.subpath(2, p.getNameCount()).toString();
        return share.list(pathUnderShare).stream()
                .filter(info -> !IGNORED_DOCUMENTS.contains(info.getFileName()))
                .map(info -> new SmbjEntry(info, share.folderExists(Paths.get(pathUnderShare, info.getFileName()).toString())))
                .collect(Collectors.toList());
    }

    @Override
    public ParcelFileDescriptor openProxyFile(String uri, String mode) {
        Path p = Paths.get(uri);
        SmbConnectionDetails smbConnectionDetails = new SmbConnectionDetails(
                p.getName(0).toString(),
                AuthenticationContext.anonymous(),
                p.getName(1).toString()
        );

        Path pathUnderShare = p.getNameCount() <= 2 ? Paths.get("") : p.subpath(2, p.getNameCount());
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
}
