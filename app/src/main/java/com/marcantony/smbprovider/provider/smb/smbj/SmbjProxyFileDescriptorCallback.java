package com.marcantony.smbprovider.provider.smb.smbj;

import android.os.ProxyFileDescriptorCallback;
import android.util.Log;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import java.io.Closeable;
import java.io.IOException;
import java.util.EnumSet;

public class SmbjProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private final Connection connection;
    private final Session session;
    private final DiskShare diskShare;
    private final File file;

    public SmbjProxyFileDescriptorCallback(SMBClient client, String host, String share, String path, AuthenticationContext auth) throws IOException {
        connection = client.connect(host);
        session = connection.authenticate(auth);
        diskShare = (DiskShare) session.connectShare(share);
        file = diskShare.openFile(
                path,
                EnumSet.of(AccessMask.FILE_READ_DATA),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null
        );
    }

    @Override
    public long onGetSize() {
        return file.getFileInformation().getStandardInformation().getEndOfFile();
    }

    @Override
    public int onRead(long offset, int size, byte[] data) {
        return Math.max(file.read(data, offset, 0, size), 0);
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) {
        return file.write(data, offset, 0, size);
    }

    @Override
    public void onFsync() {
        // do nothing
    }

    @Override
    public void onRelease() {
        file.close();
        close(diskShare);
        close(session);
        close(connection);
    }

    private void close(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            Log.e("smbj proxy file callback", "failed to close resource", e);
        }
    }
}
