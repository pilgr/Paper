package io.paperdb;

import android.content.Context;

import java.util.Collection;
import java.util.List;

public class Paper {
    static final String TAG = "paperdb";

    private static final String DEFAULT_DB_NAME = "paperdb";

    private static Paper INSTANCE;

    private final DbStorageHolder mStorageHolder;

    public static void init(Context context) {
        INSTANCE = new Paper(context);
    }

    public static void destroy(Context context) {
        new Paper(context).mStorageHolder.get().destroy();
    }

    public static <T> void insert(String tableName, Collection<T> items) {
        INSTANCE.mStorageHolder.get().insert(tableName, items);
    }

    public static <T> List<T> select(String tableName) {
        return INSTANCE.mStorageHolder.get().select(tableName);
    }

    public static boolean exist(String tableName) {
        return INSTANCE.mStorageHolder.get().exist(tableName);
    }

    public static void delete(String tableName) {
        INSTANCE.mStorageHolder.get().deleteIfExists(tableName);
    }

    private Paper(Context context) {
        this(context, DEFAULT_DB_NAME);
    }

    private Paper(Context context, String dbName) {
        mStorageHolder = new DbStorageHolder(context.getApplicationContext(), dbName);
    }

    /**
     * Initializes storage lazily
     */
    private static class DbStorageHolder {
        private DbStorageBase mStorage = null;
        private final Context mContext;
        private final String mDbName;

        public DbStorageHolder(Context context, String dbName) {
            mContext = context;
            mDbName = dbName;
        }

        private DbStorageBase get() {
            if (mStorage == null) {
                mStorage = new DbStoragePlainFile(mContext, mDbName);
            }
            return mStorage;
        }
    }
}
