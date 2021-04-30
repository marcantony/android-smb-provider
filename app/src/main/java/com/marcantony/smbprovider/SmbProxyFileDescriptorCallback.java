package com.marcantony.smbprovider;

import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;
import android.system.OsConstants;
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
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.SmbConstants;
import jcifs.context.SingletonContext;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
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

//    private final Future<SmbRandomAccessFile> file;

//    public SmbProxyFileDescriptorCallback(String url, String mode) {
//        file = Executors.newSingleThreadExecutor().submit(() ->
//                new SmbRandomAccessFile(url, mode, SmbConstants.DEFAULT_SHARING, SingletonContext.getInstance()));
//    }

    @Override
    public long onGetSize() throws ErrnoException {
        try {
            return file.length();
        } catch (SmbException e) {
            throw translateSmbException(e);
        }
    }

    @Override
    public int onRead(long offset, int size, byte[] data) throws ErrnoException {
        try {
            file.seek(offset);
            return file.read(data, 0, size);
        } catch (SmbException e) {
            throw translateSmbException(e);
        }
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
        try {
            file.seek(offset);
            file.write(data, 0, size);
            return size;
        } catch (SmbException e) {
            throw translateSmbException(e);
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
            Log.e(TAG, "got error when trying to close file", e);
        } finally {
            onCloseCallbackOption.ifPresent(Runnable::run);
        }
    }

    private ErrnoException translateSmbException(SmbException e) {
        Log.e(TAG, "got NTSTATUS: " + e.getNtStatus());
        int errno = OsConstants.EIO;
        return new ErrnoException("SmbProxyFile", errno, e);
    }
}
