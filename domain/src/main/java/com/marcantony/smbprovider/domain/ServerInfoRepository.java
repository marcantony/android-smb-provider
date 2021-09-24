package com.marcantony.smbprovider.domain;

import com.marcantony.smbprovider.domain.ServerInfo;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public interface ServerInfoRepository {
    Flowable<List<ServerInfo>> getServers();

    Flowable<List<ServerInfo>> getEnabledServers();

    ServerInfo getServerInfo(int id);

    void addServer(ServerInfo info);

    void updateServer(ServerInfo info);

    void deleteServer(ServerInfo info);

    void addOrUpdateServer(ServerInfo info);
}
