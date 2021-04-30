package com.marcantony.smbprovider;

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
import com.hierynomus.smbj.share.Share;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SmbProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private static final String TAG = "smb callback";

    private final File file;
    private final Optional<Runnable> onCloseCallbackOption;

    public SmbProxyFileDescriptorCallback(File file) {
        this(file, null);
    }

    public SmbProxyFileDescriptorCallback(File file, Runnable onCloseCallback) {
        this.file = file;
        onCloseCallbackOption = Optional.ofNullable(onCloseCallback);
    }

    @Override
    public long onGetSize() {
        return file.getFileInformation().getStandardInformation().getEndOfFile();
    }

    @Override
    public int onRead(long offset, int size, byte[] data) {
        return file.read(data, offset, 0, size);
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
        Log.d(TAG, "closing file: " + file.getFileName());
        close(file);

        onCloseCallbackOption.ifPresent(Runnable::run);
    }

    private static void close(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            Log.e("Smb file callback", "could not close resource", e);
        }
    }
}
