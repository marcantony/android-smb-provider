package com.marcantony.smbprovider;

public class ServerInfo {

    public final String host;
    public final String share;
    public final String username;
    public final String password;
    private boolean isEnabled;

    public ServerInfo(String host, String share, String username, String password) {
        if (host == null) {
            throw new NullPointerException("host cannot be null");
        }
        this.host = host;
        this.share = nullIfEmpty(share);
        this.username = nullIfEmpty(username);
        this.password = nullIfEmpty(password);
        isEnabled = true;
    }

    private String nullIfEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}