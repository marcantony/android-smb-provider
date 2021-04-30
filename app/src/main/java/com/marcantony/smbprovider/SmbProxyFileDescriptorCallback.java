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

import jcifs.smb.SmbException;
import jcifs.smb.SmbRandomAccessFile;

public class SmbProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private static final String TAG = "smb callback";

    private final SmbRandomAccessFile file;
    private final Optional<Runnable> onCloseCallbackOption;

    public SmbProxyFileDescriptorCallback(SmbRandomAccessFile file) {
        this(file, null);
    }

    public SmbProxyFileDescriptorCallback(SmbRandomAccessFile file, Runnable onCloseCallback) {
        this.file = file;
        onCloseCallbackOption = Optional.ofNullable(onCloseCallback);
    }

    @Override
    public long onGetSize() {
        try {
            Log.d(TAG, "file size: " + file.length());
            return file.length();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onRead(long offset, int size, byte[] data) {
        Log.d(TAG, String.format("reading %d bytes at pos %d with buffer length %d", size, offset, data.length));
        try {
            int read = file.read(data, Math.toIntExact(offset), size);
            Log.d(TAG, String.format("read %d bytes", read));
            return read;
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) {
        try {
            file.write(data, Math.toIntExact(offset), size);
            return size;
        } catch (SmbException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onFsync() {
        // do nothing
    }

    @Override
    public void onRelease() {
        try {
            file.close();
        } catch (SmbException e) {
            throw new RuntimeException(e);
        } finally {
            onCloseCallbackOption.ifPresent(Runnable::run);
        }
    }
}
