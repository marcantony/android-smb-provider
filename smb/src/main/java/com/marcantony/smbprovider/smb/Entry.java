package com.marcantony.smbprovider.smb;

public interface Entry {

    String getName();
    EntryStats getStats();
    boolean isDirectory();
    void close();

}
