package com.marcantony.smbprovider.data;

import android.content.Context;

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

    public void addServer(ServerInfo info) {
        serverInfoDao.insert(info).subscribeOn(Schedulers.io()).subscribe();
    }

}
