package com.marcantony.smbprovider.provider;

import jcifs.Credentials;

public class ServerAuthentication {

    public final String username;
    public final String password;

    public ServerAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
