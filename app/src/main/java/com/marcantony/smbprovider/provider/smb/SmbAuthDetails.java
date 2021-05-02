package com.marcantony.smbprovider.provider.smb;

import androidx.annotation.NonNull;

import java.util.Optional;

public class SmbAuthDetails {

    public final Optional<String> domain;
    public final String username;
    public final Optional<String> password;

    public SmbAuthDetails(String username) {
        this(username, null, null);
    }

    public SmbAuthDetails(String username, String password) {
        this(username, password, null);
    }

    public SmbAuthDetails(String username, String password, String domain) {
        if (username == null) {
            throw new NullPointerException("username cannot be null");
        }
        this.username = username;
        this.password = Optional.ofNullable(password);
        this.domain = Optional.ofNullable(domain);
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();

        domain.ifPresent(d -> sb.append(d).append(';'));
        sb.append(username);
        password.ifPresent(p -> sb.append(':').append(p));

        return sb.toString();
    }

}
