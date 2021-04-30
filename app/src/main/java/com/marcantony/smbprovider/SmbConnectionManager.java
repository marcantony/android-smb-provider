package com.marcantony.smbprovider;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SmbConnectionManager {

    private final SMBClient smbClient;
    private final Map<String, Connection> connections;
    private final Map<String, Session> sessions;
    private final Map<String, DiskShare> shares;

    public SmbConnectionManager(SMBClient smbClient) {
        this.smbClient = smbClient;
        connections = new HashMap<>();
        sessions = new HashMap<>();
        shares = new HashMap<>();
    }

    public DiskShare getShare(SmbConnectionDetails details) {
        Connection c = connections.computeIfAbsent(details.hostname, k -> {
            try {
                return smbClient.connect(k);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        });

        Session s = sessions.computeIfAbsent(details.hostname + "|" + details.authenticationContext.getUsername(),
                k -> c.authenticate(details.authenticationContext));

        return shares.computeIfAbsent(details.hostname + "|" + details.authenticationContext.getUsername() + "|" + details.share,
                k -> (DiskShare) s.connectShare(details.share));
    }

}
