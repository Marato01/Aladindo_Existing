package com.reeman.delige.repository.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.reeman.delige.BuildConfig;
import com.reeman.delige.repository.dao.DeliveryRecordDao;
import com.reeman.delige.repository.entities.DeliveryRecord;

@Database(entities = {DeliveryRecord.class}, exportSchema = false, version = 1)
public abstract class AppDataBase extends RoomDatabase {

    private static final String DB_NAME = "db_delivery_record";

    private static volatile AppDataBase sInstance;

    public abstract DeliveryRecordDao deliveryRecordDao();

    public static AppDataBase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppDataBase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context, AppDataBase.class, DB_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                }

                                @Override
                                public void onDestructiveMigration(SupportSQLiteDatabase db) {
                                }
                            })
                            .build();
                }
            }
        }
        return sInstance;
    }


}
