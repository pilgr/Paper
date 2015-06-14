package io.paperdb;

import android.content.Context;

public class Paper {
    static final String TAG = "paperdb";

    private static final String DEFAULT_DB_NAME = "io.paperdb";

    private static Paper INSTANCE;

    private final Storage mStorage;

    public static Paper init(Context context) {
        return INSTANCE = new Paper(context);
    }

    public static void destroy() {
        INSTANCE.mStorage.destroy();
        INSTANCE = null;
    }

    public static <T> Paper put(String key, T value) {
        if (value == null) {
            INSTANCE.mStorage.deleteIfExists(key);
        } else {
            INSTANCE.mStorage.insert(key, value);
        }
        return INSTANCE;
    }

    public static <T> T get(String key) {
        return get(key, null);
    }

    public static <T> T get(String key, T defaultValue) {
        T value = INSTANCE.mStorage.select(key);
        return value == null ? defaultValue : value;
    }

    public static boolean exist(String tableName) {
        return INSTANCE.mStorage.exist(tableName);
    }

    public static Paper delete(String tableName) {
        INSTANCE.mStorage.deleteIfExists(tableName);
        return INSTANCE;
    }

    private Paper(Context context) {
        this(context, DEFAULT_DB_NAME);
    }

    private Paper(Context context, String dbName) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName);
    }

}
