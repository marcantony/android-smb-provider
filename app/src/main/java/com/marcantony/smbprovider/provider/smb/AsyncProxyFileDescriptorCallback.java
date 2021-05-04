package com.marcantony.smbprovider.provider.smb;

import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AsyncProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private final Future<ProxyFileDescriptorCallback> future;

    public AsyncProxyFileDescriptorCallback(Callable<ProxyFileDescriptorCallback> factory, ExecutorService executor) {
        this.future = executor.submit(factory);
    }

    @Override
    public long onGetSize() throws ErrnoException {
        return get().onGetSize();
    }

    @Override
    public int onRead(long offset, int size, byte[] data) throws ErrnoException {
        return get().onRead(offset, size, data);
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
        return get().onWrite(offset, size, data);
    }

    @Override
    public void onFsync() throws ErrnoException {
        get().onFsync();
    }

    @Override
    public void onRelease() {
        get().onRelease();
    }

    private ProxyFileDescriptorCallback get() {
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
