package com.marcantony.smbprovider.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface ServerInfoDao {

    @Query("SELECT * FROM ServerInfo")
    Flowable<List<ServerInfo>> getAll();

    @Query("SELECT * FROM ServerInfo where isEnabled = 1")
    Flowable<List<ServerInfo>> getEnabled();

    @Insert
    Completable insert(ServerInfo server);

}
