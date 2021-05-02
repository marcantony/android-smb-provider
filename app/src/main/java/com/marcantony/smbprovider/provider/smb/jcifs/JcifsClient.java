package com.marcantony.smbprovider.provider.smb.jcifs;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.util.Log;

import com.marcantony.smbprovider.provider.smb.Client;
import com.marcantony.smbprovider.provider.smb.Entry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.SmbFile;

public class JcifsClient implements Client {

    private static final String TAG = "JcifsClient";

    private final StorageManager storageManager;
    private final HandlerThread handlerThread;

    public JcifsClient(StorageManager storageManager) {
        if (storageManager == null) {
            throw new NullPointerException("storage manager cannot be null");
        }
        this.storageManager = storageManager;

        handlerThread = new HandlerThread("smb");
        handlerThread.start();
    }

    @Override
    public Iterable<Entry> listDir(String uri) {
        try {
            Log.d(TAG, "getting children of: " + uri);
            List<Entry> children = new LinkedList<>();

            SmbFile file = new SmbFile("smb://" + uri, SingletonContext.getInstance());
            file.children().forEachRemaining(child -> children.add(new JcifsEntry(child)));
            file.close();

            return children;
        } catch (MalformedURLException | CIFSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ParcelFileDescriptor openProxyFile(String uri, String mode) {
        try {
            return storageManager.openProxyFileDescriptor(
                    ParcelFileDescriptor.parseMode(mode),
                    new JcifsProxyFileDescriptorCallback("smb://" + uri, mode),
                    Handler.createAsync(handlerThread.getLooper())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}