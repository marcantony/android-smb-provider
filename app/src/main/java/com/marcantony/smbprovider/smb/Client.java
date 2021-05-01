package com.marcantony.smbprovider.smb;

import android.os.ParcelFileDescriptor;

public interface Client {

    /**
     * Get entry information. Avoid actually opening a file for reading.
     * @param uri The URI pointing to the entry to read.
     * @return Stats about the entry.
     */
    EntryStats stat(String uri);

    /**
     * List the entries inside a directory.
     * @param uri The directory to list.
     * @return An iterable of the entires inside the directory.
     */
    Iterable<Entry> listDir(String uri);

    /**
     * Open an Android ParcelFileDescriptor as a proxy for the SMB file.
     * @param uri The file to open.
     * @param mode "r", "w", or "rw"
     * @return A proxy file descriptor for the opened file.
     */
    ParcelFileDescriptor openProxyFile(String uri, String mode);

}
