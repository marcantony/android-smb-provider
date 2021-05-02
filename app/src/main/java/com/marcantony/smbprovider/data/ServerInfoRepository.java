package com.marcantony.smbprovider.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerInfoRepository {

    private final List<ServerInfo> servers;

    public ServerInfoRepository() {
        this.servers = new ArrayList<>();
        servers.add(new ServerInfo("raspberrypi", "Media", null, null));
        servers.add(new ServerInfo("raspberrypi", "foo", null, null));
    }

    private static ServerInfoRepository instance;
    public static ServerInfoRepository getInstance() {
        if (instance == null) {
            instance = new ServerInfoRepository();
        }
        return instance;
    }

    public List<ServerInfo> getServers() {
        return Collections.unmodifiableList(servers);
    }

    public void addServer(ServerInfo info) {
        servers.add(info);
    }

}
