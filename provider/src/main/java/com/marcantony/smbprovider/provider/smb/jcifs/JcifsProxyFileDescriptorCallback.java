package com.marcantony.smbprovider.provider.smb.jcifs;

import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;

import java.net.MalformedURLException;

import jcifs.CIFSContext;
import jcifs.SmbConstants;
import jcifs.smb.SmbException;
import jcifs.smb.SmbRandomAccessFile;

public class JcifsProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private static final String TAG = "smb callback";

    private final SmbRandomAccessFile file;

    public JcifsProxyFileDescriptorCallback(String url, String mode, CIFSContext context) {
        try {
            file = new SmbRandomAccessFile(url, mode, SmbConstants.DEFAULT_SHARING, context);
        } catch (SmbException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

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
        }
    }

    private ErrnoException translateSmbException(SmbException e) {
        Log.e(TAG, "got NTSTATUS: " + e.getNtStatus());
        int errno = OsConstants.EIO;
        return new ErrnoException("SmbProxyFile", errno, e);
    }
}
