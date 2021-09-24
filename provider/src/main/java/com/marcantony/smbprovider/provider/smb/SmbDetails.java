package com.marcantony.smbprovider.provider.smb;

import java.util.Optional;

public class SmbDetails {

    public final String hostname;
    public final String share;
    public final Optional<SmbAuthDetails> authDetails;

    public SmbDetails(String hostname, String share) {
        this(hostname, share, null);
    }

    public SmbDetails(
            String hostname,
            String share,
            SmbAuthDetails authDetails
    ) {
        if (hostname == null) {
            throw new NullPointerException("hostname cannot be null");
        }
        if (share == null) {
            throw new NullPointerException("share cannot be null");
        }

        this.hostname = hostname;
        this.share = share;
        this.authDetails = Optional.ofNullable(authDetails);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("smb://");
        authDetails.ifPresent(ad -> sb.append(ad).append('@'));
        sb.append(hostname).append('/');
        sb.append(share).append('/');

        return sb.toString();
    }
}
