package com.marcantony.smbprovider.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.marcantony.smbprovider.domain.ServerInfo;

@Database(entities = {ServerInfo.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ServerInfoDao serverInfoDao();

    public static AppDatabase getInstance(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "smb-provider").build();
    }
}
