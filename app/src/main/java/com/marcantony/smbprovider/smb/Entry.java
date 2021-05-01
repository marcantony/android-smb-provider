package com.marcantony.smbprovider.smb;

public interface Entry {

    String getName();
    String getFullPath();
    EntryStats getStats();
    void close();

}
