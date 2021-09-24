package com.marcantony.smbprovider.provider.smb;

public interface Entry {

    String getName();
    EntryStats getStats();
    boolean isDirectory();
    void close();

}
