package io.paperdb;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

/**
 * Fast NoSQL data storage with auto-upgrade support to save any types of Plain Old Java Objects or
 * collections using Kryo serialization.
 * <p/>
 * Every custom class must have no-arg constructor. Common classes supported out of the box.
 * <p/>
 * Auto upgrade works in a way that removed object's fields are ignored on read and new fields
 * have their default values on create class instance.
 * <p/>
 * Each object is saved in separate Paper file with name like object_key.pt.
 * All Paper files are created in the /files/io.paperdb dir in app's private storage.
 */
public class Paper {
    static final String TAG = "paperdb";

    public static final String DEFAULT_DB_NAME = "io.paperdb";

    private static Paper INSTANCE;

    private final Storage mStorage;

    /**
     * Lightweight method to init Paper instance. Should be executed in {@link Application#onCreate()}
     * or {@link android.app.Activity#onCreate(Bundle)}.
     * <p/>
     * All {@link #put(String, Object)} and {@link #get(String)} methods should be called after this
     * method is executed.
     *
     * @param context context, uses to get application context
     */
    public static void init(Context context) {
        INSTANCE = new Paper(context);
    }

    /**
     * Clears all data saved by Paper. Can be used even when Paper yet not initialized
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

    /**
     * Saves any types of POJOs or collections in Paper storage.
     *
     * @param key   object key is used as part of object's file name
     * @param value object to save, must have no-arg constructor
     * @param <T>   object type
     * @return this Paper instance
     */
    public static <T> Paper put(String key, T value) {
        if (value == null) {
            INSTANCE.mStorage.deleteIfExists(key);
        } else {
            INSTANCE.mStorage.insert(key, value);
        }
        return INSTANCE;
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key object key to read
     * @return the saved object instance or null
     */
    public static <T> T get(String key) {
        return get(key, null);
    }

    /**
     * Instantiates saved object using original object class (e.g. LinkedList). Support limited
     * backward and forward compatibility: removed fields are ignored, new fields have their
     * default values.
     * <p/>
     * All instantiated objects must have no-arg constructors.
     *
     * @param key          object key to read
     * @param defaultValue will be returned if key doesn't exist
     * @return the saved object instance or null
     */
    public static <T> T get(String key, T defaultValue) {
        T value = INSTANCE.mStorage.select(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Check if an object with the given key is saved in Paper storage.
     *
     * @param key object key
     * @return true if object with given key exists in Paper storage, false otherwise
     */
    public static boolean exist(String key) {
        return INSTANCE.mStorage.exist(key);
    }

    /**
     * Delete saved object for given key if it is exist.
     *
     * @param key object key
     * @return this Paper instance
     */
    public static Paper delete(String key) {
        INSTANCE.mStorage.deleteIfExists(key);
        return INSTANCE;
    }

    private Paper(Context context) {
        this(context, DEFAULT_DB_NAME);
    }

    private Paper(Context context, String dbName) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext().getFilesDir(), dbName);
    }

}
