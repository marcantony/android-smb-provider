package com.marcantony.smbprovider.smb.smbj;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.marcantony.smbprovider.smb.Entry;
import com.marcantony.smbprovider.smb.EntryStats;

public class SmbjEntry implements Entry {

    private final FileIdBothDirectoryInformation info;

    public SmbjEntry(FileIdBothDirectoryInformation info) {
        this.info = info;
    }

    @Override
    public String getName() {
        return info.getFileName();
    }

    @Override
    public EntryStats getStats() {
        return new EntryStats(getName(), info.getEndOfFile(), info.getLastWriteTime().toEpochMillis());
    }

    @Override
    public void close() {
        // do nothing
    }
}
