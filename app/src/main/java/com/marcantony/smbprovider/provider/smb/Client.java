package com.marcantony.smbprovider.provider.smb;

import android.os.ParcelFileDescriptor;

import com.marcantony.smbprovider.provider.ServerAuthentication;

public interface Client {

    /**
     * List the entries inside a directory.
     * @param uri The directory to list.
     * @return An iterable of the entires inside the directory.
     */
    Iterable<Entry> listDir(String uri, ServerAuthentication auth);

    /**
     * Open an Android ParcelFileDescriptor as a proxy for the SMB file.
     * @param uri The file to open.
     * @param mode "r", "w", or "rw"
     * @return A proxy file descriptor for the opened file.
     */
    ParcelFileDescriptor openProxyFile(String uri, ServerAuthentication auth, String mode);

}
