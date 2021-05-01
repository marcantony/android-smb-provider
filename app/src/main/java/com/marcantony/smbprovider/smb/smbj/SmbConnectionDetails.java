package com.marcantony.smbprovider.smb.smbj;

import com.hierynomus.smbj.auth.AuthenticationContext;

public class SmbConnectionDetails {

    public final String hostname;
    public final AuthenticationContext authenticationContext;
    public final String share;

    public SmbConnectionDetails(
            String hostname,
            AuthenticationContext authenticationContext,
            String share
    ) {
        if (hostname == null || authenticationContext == null || share == null) {
            throw new NullPointerException("arguments cannot be null");
        }
        this.hostname = hostname;
        this.authenticationContext = authenticationContext;
        this.share = share;
    }
}
