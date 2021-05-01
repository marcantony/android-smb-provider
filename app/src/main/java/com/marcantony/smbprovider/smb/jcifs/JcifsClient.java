package com.marcantony.smbprovider.smb.jcifs;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.util.Log;

import com.marcantony.smbprovider.smb.Client;
import com.marcantony.smbprovider.smb.Entry;
import com.marcantony.smbprovider.smb.EntryStats;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.SmbFile;

public class JcifsClient implements Client {

    private static final String TAG = "JcifsClient";

    private final StorageManager storageManager;
    private final HandlerThread handlerThread;
    private final ExecutorService executor;

    public JcifsClient(StorageManager storageManager, ExecutorService executor) {
        if (storageManager == null) {
            throw new NullPointerException("storage manager cannot be null");
        }
        this.storageManager = storageManager;

        if (executor == null) {
            throw new NullPointerException("executor cannot be null");
        }
        this.executor = executor;

        handlerThread = new HandlerThread("smb");
        handlerThread.start();
    }

    @Override
    public EntryStats stat(String uri) {
        return new EntryStats(uri);
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
                    new JcifsProxyFileDescriptorCallback("smb://" + uri, mode, executor),
                    Handler.createAsync(handlerThread.getLooper())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
