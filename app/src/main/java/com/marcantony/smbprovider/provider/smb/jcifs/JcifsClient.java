package com.marcantony.smbprovider.provider.smb.jcifs;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.util.Log;

import com.marcantony.smbprovider.provider.ServerAuthentication;
import com.marcantony.smbprovider.provider.smb.Client;
import com.marcantony.smbprovider.provider.smb.Entry;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
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
    public Iterable<Entry> listDir(String uri, ServerAuthentication auth) {
        try {
            Log.d(TAG, "getting children of: " + uri);
            List<Entry> children = new LinkedList<>();

            SmbFile file = new SmbFile("smb://" + uri, getContext(auth));
            file.children().forEachRemaining(child -> children.add(new JcifsEntry(child)));
            file.close();

            return children;
        } catch (MalformedURLException | CIFSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ParcelFileDescriptor openProxyFile(String uri, ServerAuthentication auth, String mode) {
        try {
            return storageManager.openProxyFileDescriptor(
                    ParcelFileDescriptor.parseMode(mode),
                    new JcifsProxyFileDescriptorCallback("smb://" + uri, mode, getContext(auth)),
                    Handler.createAsync(handlerThread.getLooper())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CIFSContext getContext(ServerAuthentication auth) {
        CIFSContext baseContext = SingletonContext.getInstance();
        return auth.username == null ? baseContext.withAnonymousCredentials() :
                baseContext.withCredentials(new DumbNtlmPasswordAuthenticator(auth.username, auth.password));
    }

    private static class DumbNtlmPasswordAuthenticator extends NtlmPasswordAuthenticator {
        public DumbNtlmPasswordAuthenticator(String username, String password) {
            // Call a protected constructor in NtlmPasswordAuthenticator because the exposed
            // one tries to be "too" smart and parse a domain from the username.
            // This ends up breaking functionality for usernames with `@` in them.
            super(null, null, username, password, null);
        }
    }
}
