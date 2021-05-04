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
import java.net.URI;
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
    public Iterable<Entry> listDir(URI uri) {
        try {
            Log.d(TAG, "getting children of: " + uri);
            List<Entry> children = new LinkedList<>();

            SmbFile file = new SmbFile(parseUri(uri), getContext(uri));
            file.children().forEachRemaining(child -> children.add(new JcifsEntry(child)));
            file.close();

            return children;
        } catch (MalformedURLException | CIFSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ParcelFileDescriptor openProxyFile(URI uri, String mode) {
        try {
            return storageManager.openProxyFileDescriptor(
                    ParcelFileDescriptor.parseMode(mode),
                    new JcifsProxyFileDescriptorCallback(parseUri(uri), mode, getContext(uri)),
                    Handler.createAsync(handlerThread.getLooper())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseUri(URI uri) {
        StringBuilder sb = new StringBuilder();
        sb
                .append(uri.getScheme())
                .append("://")
                .append(uri.getHost());

        if (uri.getPort() != -1) {
            sb.append(':').append(uri.getPort());
        }

        if (uri.getPath() != null) {
            sb.append(uri.getPath());
        } else {
            sb.append('/');
        }

        return sb.toString();
    }

    private CIFSContext getContext(URI uri) {
        String userInfo = uri.getUserInfo();

        CIFSContext baseContext = SingletonContext.getInstance();
        return userInfo == null ? baseContext.withAnonymousCredentials() :
                baseContext.withCredentials(new DumbNtlmPasswordAuthenticator(userInfo));
    }

    private static class DumbNtlmPasswordAuthenticator extends NtlmPasswordAuthenticator {
        public DumbNtlmPasswordAuthenticator(String userInfo) {
            // Call a protected constructor in NtlmPasswordAuthenticator because the exposed
            // one tries to be "too" smart and parse a domain from the username.
            // This ends up breaking functionality for usernames with `@` in them.
            super(userInfo, null, null, null, null);
        }
    }
}
