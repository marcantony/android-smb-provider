package com.marcantony.smbprovider.smb.jcifs;

import com.marcantony.smbprovider.smb.Entry;
import com.marcantony.smbprovider.smb.EntryStats;

import java.nio.file.Paths;

import jcifs.CIFSException;
import jcifs.SmbResource;

public class JcifsEntry implements Entry {

    private final SmbResource smbResource;

    public JcifsEntry(SmbResource smbResource) {
        this.smbResource = smbResource;
    }

    @Override
    public String getName() {
        return Paths.get(smbResource.getName()).getFileName().toString();
    }

    @Override
    public EntryStats getStats() {
        try {
            return new EntryStats(getName(), smbResource.length(), smbResource.lastModified());
        } catch (CIFSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        smbResource.close();
    }
}
