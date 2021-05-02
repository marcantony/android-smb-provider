package com.marcantony.smbprovider.provider.smb.smbj;

import android.os.ProxyFileDescriptorCallback;

import com.hierynomus.smbj.share.File;

public class SmbjProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private final File file;

    public SmbjProxyFileDescriptorCallback(File file) {
        this.file = file;
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
    }
}
