package com.marcantony.smbprovider;

import com.marcantony.smbprovider.smb.SmbDetails;

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
