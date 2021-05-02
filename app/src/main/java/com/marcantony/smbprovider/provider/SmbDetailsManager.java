package com.marcantony.smbprovider.provider;

import com.marcantony.smbprovider.provider.smb.SmbDetails;

import java.util.LinkedList;
import java.util.List;

public class SmbDetailsManager {

    public List<SmbDetails> getAllDetails() {
        List<SmbDetails> details = new LinkedList<>();

        details.add(new SmbDetails("raspberrypi", "Media"));
        details.add(new SmbDetails("raspberrypi", "foo"));

        return details;
    }

}
