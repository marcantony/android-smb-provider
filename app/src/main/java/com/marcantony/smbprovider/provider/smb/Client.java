package com.marcantony.smbprovider.provider.smb;

import android.os.ParcelFileDescriptor;

import java.net.URI;

public interface Client {

    /**
     * List the entries inside a directory.
     * @param uri {@link URI} of the directory to list. URI should contain any necessary user info.
     * @return An iterable of the entires inside the directory.
     */
    Iterable<Entry> listDir(URI uri);

    /**
     * Open an Android ParcelFileDescriptor as a proxy for the SMB file.
     * @param uri {@link URI} of the file to open. URI should contain any necessary user info.
     * @param mode "r", "w", or "rw"
     * @return A proxy file descriptor for the opened file.
     */
    ParcelFileDescriptor openProxyFile(URI uri, String mode);

}
