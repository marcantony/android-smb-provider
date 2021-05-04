package com.marcantony.smbprovider;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.marcantony.smbprovider.data.ServerInfo;
import com.marcantony.smbprovider.data.ServerInfoRepository;

import java.util.List;

public class ServerListViewModel extends ViewModel {

    private final LiveData<List<ServerInfo>> servers;
    private final ServerInfoRepository repository;

    public ServerListViewModel(ServerInfoRepository repository) {
        this.repository = repository;
        servers = LiveDataReactiveStreams.fromPublisher(repository.getServers());
    }

    public LiveData<List<ServerInfo>> getServers() {
        return servers;
    }

    public void addServer(ServerInfo info) {
        repository.addServer(info);
    }

    public void updateServer(ServerInfo info) {
        repository.updateServer(info);
    }

    public void deleteServer(ServerInfo info) {
        repository.deleteServer(info);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final ServerInfoRepository repository;

        public Factory(ServerInfoRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass != ServerListViewModel.class) {
                throw new IllegalArgumentException("this factory only creates ServerListViewModel");
            }
            return (T) new ServerListViewModel(repository);
        }
    }

}
