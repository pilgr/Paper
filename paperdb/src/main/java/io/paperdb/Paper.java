package io.paperdb;

import android.content.Context;

public class Paper {
    static final String TAG = "paperdb";

    private static final String DEFAULT_DB_NAME = "paperdb";

    private static Paper INSTANCE;

    private final Storage mStorage;

    public static void init(Context context) {
        INSTANCE = new Paper(context);
    }

    public static void destroy(Context context) {
        new Paper(context).mStorage.destroy();
    }

    public static <T> void put(String key, T value) {
        if (value == null) {
            INSTANCE.mStorage.deleteIfExists(key);
            return;
        }
        INSTANCE.mStorage.insert(key, value);
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

    public static void delete(String tableName) {
        INSTANCE.mStorage.deleteIfExists(tableName);
    }

    private Paper(Context context) {
        this(context, DEFAULT_DB_NAME);
    }

    private Paper(Context context, String dbName) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName);
    }

}
