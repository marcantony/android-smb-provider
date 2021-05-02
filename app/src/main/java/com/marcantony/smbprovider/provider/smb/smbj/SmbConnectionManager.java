package com.marcantony.smbprovider.provider.smb.smbj;

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

//    public DiskShare getShare(SmbConnectionDetails details) {
//        Connection c = connections.compute(details.hostname, (k, v) -> {
//            if (v != null && v.isConnected()) {
//                return v;
//            }
//
//            try {
//                return smbClient.connect(k);
//            } catch(IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        Session s = sessions.computeIfAbsent(details.hostname + "|" + details.authenticationContext.getUsername(),
//                k -> c.authenticate(details.authenticationContext));
//
//        return shares.compute(details.hostname + "|" + details.authenticationContext.getUsername() + "|" + details.share,
//                (k, v) -> v != null && v.isConnected() ? v : (DiskShare) s.connectShare(details.share));
//    }
    public DiskShare getShare(SmbConnectionDetails details) {
        try {
            return (DiskShare) smbClient
                    .connect(details.hostname)
                    .authenticate(details.authenticationContext)
                    .connectShare(details.share);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
