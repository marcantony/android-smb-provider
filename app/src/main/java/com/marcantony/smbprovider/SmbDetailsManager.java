package com.marcantony.smbprovider;

import com.marcantony.smbprovider.smb.SmbDetails;

import java.util.Collections;
import java.util.List;

public class SmbDetailsManager {

    public static List<SmbDetails> getAllDetails() {
        return Collections.singletonList(
                new SmbDetails("raspberrypi", "Media")
        );
    }

}
