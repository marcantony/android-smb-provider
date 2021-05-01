package com.marcantony.smbprovider.smb.jcifs;

import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import jcifs.SmbConstants;
import jcifs.context.SingletonContext;
import jcifs.smb.SmbException;
import jcifs.smb.SmbRandomAccessFile;

public class JcifsProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private static final String TAG = "smb callback";

    private final Future<SmbRandomAccessFile> file;

    public JcifsProxyFileDescriptorCallback(String url, String mode, ExecutorService executor) {
        file = executor.submit(() ->
                new SmbRandomAccessFile(url, mode, SmbConstants.DEFAULT_SHARING, SingletonContext.getInstance()));
    }

    @Override
    public long onGetSize() throws ErrnoException {
        try {
            return file.get().length();
        } catch (SmbException e) {
            throw translateSmbException(e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onRead(long offset, int size, byte[] data) throws ErrnoException {
        try {
            file.get().seek(offset);
            return file.get().read(data, 0, size);
        } catch (SmbException e) {
            throw translateSmbException(e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
        try {
            file.get().seek(offset);
            file.get().write(data, 0, size);
            return size;
        } catch (SmbException e) {
            throw translateSmbException(e);
        } catch (InterruptedException | ExecutionException e) {
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
            file.get().close();
        } catch (SmbException | InterruptedException | ExecutionException e) {
            Log.e(TAG, "got error when trying to close file", e);
        }
    }

    private ErrnoException translateSmbException(SmbException e) {
        Log.e(TAG, "got NTSTATUS: " + e.getNtStatus());
        int errno = OsConstants.EIO;
        return new ErrnoException("SmbProxyFile", errno, e);
    }
}
