package io.paperdb;

import android.content.Context;

public class Paper {
    static final String TAG = "paperdb";

    private static final String DEFAULT_DB_NAME = "io.paperdb";

    private static Paper INSTANCE;

    private final Storage mStorage;

    /**
     * Initializes Paper instance. Should be called before any other methods
     * except {@link #clear(Context)}.
     *
     * @param context context
     */
    public static void init(Context context) {
        INSTANCE = new Paper(context);
    }

    /**
     * Clears all data saved by Paper. Can be used when Paper yet not initialized
     * by {@link #init(Context)}
     *
     * @param context context
     */
    public static void clear(Context context) {
        if (INSTANCE == null) {
            new Paper(context).mStorage.destroy();
        } else {
            INSTANCE.mStorage.destroy();
        }
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
