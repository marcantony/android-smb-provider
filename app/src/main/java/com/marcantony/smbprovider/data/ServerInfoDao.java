package com.marcantony.smbprovider.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.marcantony.smbprovider.domain.ServerInfo;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface ServerInfoDao {

    @Query("SELECT * FROM ServerInfo")
    Flowable<List<ServerInfo>> getAll();

    @Query("SELECT * FROM ServerInfo where isEnabled = 1")
    Flowable<List<ServerInfo>> getEnabled();

    @Query("SELECT * FROM ServerInfo where id=:id")
    ServerInfo getById(int id);

    @Insert
    Completable insert(ServerInfo server);

    @Update
    Completable update(ServerInfo server);

    @Delete
    Completable delete(ServerInfo server);

}
