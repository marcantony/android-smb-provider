package com.marcantony.smbprovider.persistence;

import android.content.Context;

import com.marcantony.smbprovider.domain.ServerInfo;
import com.marcantony.smbprovider.domain.ServerInfoRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RoomServerInfoRepository implements ServerInfoRepository {

    private final ServerInfoDao serverInfoDao;

    public RoomServerInfoRepository(ServerInfoDao serverInfoDao) {
        this.serverInfoDao = serverInfoDao;
    }

    private static ServerInfoRepository instance;
    public static ServerInfoRepository getInstance(Context context) {
        if (instance == null) {
            AppDatabase db = AppDatabase.getInstance(context);
            instance = new RoomServerInfoRepository(db.serverInfoDao());
        }
        return instance;
    }

    @Override
    public Flowable<List<ServerInfo>> getServers() {
        return serverInfoDao.getAll();
    }

    @Override
    public Flowable<List<ServerInfo>> getEnabledServers() {
        return serverInfoDao.getEnabled();
    }

    @Override
    public ServerInfo getServerInfo(int id) {
        return serverInfoDao.getById(id);
    }

    @Override
    public void addServer(ServerInfo info) {
        serverInfoDao.insert(info).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void updateServer(ServerInfo info) {
        serverInfoDao.update(info).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void deleteServer(ServerInfo info) {
        serverInfoDao.delete(info).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void addOrUpdateServer(ServerInfo info) {
        if (info.id == ServerInfo.ID_UNSET) {
            addServer(info);
        } else {
            updateServer(info);
        }
    }

}
