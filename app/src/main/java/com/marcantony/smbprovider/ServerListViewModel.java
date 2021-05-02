package com.marcantony.smbprovider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ServerListViewModel extends ViewModel {
    private final MutableLiveData<List<ServerInfo>> servers = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<ServerInfo>> getServers() {
        return servers;
    }

    public void addServer(ServerInfo info) {
        servers.getValue().add(info);
        servers.setValue(servers.getValue());
    }

}
