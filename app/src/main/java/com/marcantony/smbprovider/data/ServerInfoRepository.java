package com.marcantony.smbprovider.data;

import android.content.Context;

import com.marcantony.smbprovider.domain.ServerInfo;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ServerInfoRepository {

    private final ServerInfoDao serverInfoDao;

    public ServerInfoRepository(ServerInfoDao serverInfoDao) {
        this.serverInfoDao = serverInfoDao;
    }

    private static ServerInfoRepository instance;
    public static ServerInfoRepository getInstance(Context context) {
        if (instance == null) {
            AppDatabase db = AppDatabase.getInstance(context);
            instance = new ServerInfoRepository(db.serverInfoDao());
        }
        return instance;
    }

    public Flowable<List<ServerInfo>> getServers() {
        return serverInfoDao.getAll();
    }

    public Flowable<List<ServerInfo>> getEnabledServers() {
        return serverInfoDao.getEnabled();
    }

    public ServerInfo getServerInfo(int id) {
        return serverInfoDao.getById(id);
    }

    public void addServer(ServerInfo info) {
        serverInfoDao.insert(info).subscribeOn(Schedulers.io()).subscribe();
    }

    public void updateServer(ServerInfo info) {
        serverInfoDao.update(info).subscribeOn(Schedulers.io()).subscribe();
    }

    public void deleteServer(ServerInfo info) {
        serverInfoDao.delete(info).subscribeOn(Schedulers.io()).subscribe();
    }

    public void addOrUpdateServer(ServerInfo info) {
        if (info.id == ServerInfo.ID_UNSET) {
            addServer(info);
        } else {
            updateServer(info);
        }
    }

}
