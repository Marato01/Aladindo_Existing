package com.reeman.delige.repository;


import com.reeman.delige.repository.db.AppDataBase;
import com.reeman.delige.repository.entities.DeliveryRecord;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class DbRepository {
    private static DbRepository sInstance;
    private final AppDataBase database;

    public DbRepository(AppDataBase database) {
        this.database = database;
    }

    public static DbRepository getInstance(final AppDataBase database) {
        if (sInstance == null) {
            synchronized (DbRepository.class) {
                if (sInstance == null) {
                    sInstance = new DbRepository(database);
                }
            }
        }
        return sInstance;
    }

    public long addDeliveryRecord(DeliveryRecord record) {
        return database.deliveryRecordDao().addDeliveryRecord(record);
    }

    public Single<List<DeliveryRecord>> getAllDeliveryRecords() {
        return database.deliveryRecordDao().getAllDeliveryRecords();
    }

    public void deleteAllRecords() {
        database.deliveryRecordDao().deleteAllRecords();
    }

}
