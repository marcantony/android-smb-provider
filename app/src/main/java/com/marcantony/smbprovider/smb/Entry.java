package com.marcantony.smbprovider.smb;

import java.io.Closeable;

public interface Entry extends Closeable {

    String getName();
    String getFullPath();
    EntryStats getStats();

}
