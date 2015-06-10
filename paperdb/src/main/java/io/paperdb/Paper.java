package io.paperdb;

import android.content.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    public static <T> void putList(String key, List<T> list) {
        putCollection(key, list);
    }

    //TODO tests
    public static <T> void putSet(String key, Set<T> set) {
        putCollection(key, set);
    }

    public static <T> List<T> getList(String key) {
        List<T> list = INSTANCE.mStorageHolder.get().select(key, null);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    //TODO tests
    public static <T> Set<T> getSet(String key) {
        Set<T> set = INSTANCE.mStorageHolder.get().select(key, null);
        if (set == null) {
            set = Collections.emptySet();
        }
        return set;
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

    private static <T> void putCollection(String key, Collection<T> items) {
        if (items == null || items.size() == 0) {
            INSTANCE.mStorageHolder.get().deleteIfExists(key);
            return;
        }
        INSTANCE.mStorageHolder.get().insert(key, items);
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
