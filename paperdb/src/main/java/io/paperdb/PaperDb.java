package io.paperdb;

import android.content.Context;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class PaperDb {
    static final String TAG = "paperdb";

    private static final String DEFAULT_DB_NAME = "paperdb";

    private final DbStorageBase mStorage;

    public PaperDb(Context context) {
        this(context, DEFAULT_DB_NAME);
    }

    public PaperDb(Context context, String dbName) {
        mStorage = createStorageInstance(context, dbName);
    }

    public static void destroy(Context context) {
        destroy(context, DEFAULT_DB_NAME);
    }

    public static void destroy(Context context, String dbName) {
        DbStorageBase storage = createStorageInstance(context, dbName);
        storage.destroy(context, dbName);
    }

    private static DbStorageBase createStorageInstance(Context context, String dbName) {
        return new DbStoragePlainFile(context, dbName);
    }

    public <E extends Serializable> void insert(String tableName, Collection<E> items) {
        mStorage.insert(tableName, items);
    }

    public <E extends Serializable> List<E> select(String tableName) {
        return mStorage.select(tableName);
    }

    public boolean exist(String tableName) {
        return mStorage.exist(tableName);
    }

    public void delete(String tableName) {
        mStorage.deleteIfExists(tableName);
    }

}
