package com.marcantony.smbprovider;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.marcantony.smbprovider.data.ServerInfo;
import com.marcantony.smbprovider.data.ServerInfoRepository;

import java.util.List;

public class ServerListViewModel extends ViewModel {

    private final MutableLiveData<List<ServerInfo>> servers;
    private final ServerInfoRepository repository;

    public ServerListViewModel(ServerInfoRepository repository) {
        this.repository = repository;
        servers = new MutableLiveData<>();
        loadServers();
    }

    public LiveData<List<ServerInfo>> getServers() {
        return servers;
    }

    public void addServer(ServerInfo info) {
        repository.addServer(info);
        loadServers();
    }

    private void loadServers() {
        servers.setValue(repository.getServers());
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
