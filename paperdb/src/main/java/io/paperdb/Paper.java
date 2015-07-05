package io.paperdb;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.util.concurrent.ConcurrentHashMap;

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

    private static final String DEFAULT_DB_NAME = "io.paperdb";

    private final Storage mStorage;

    private static Context mContext;

    private static ConcurrentHashMap<String, Paper> mPaperMap;

    /**
     * Lightweight method to init Paper instance. Should be executed in {@link Application#onCreate()}
     * or {@link android.app.Activity#onCreate(Bundle)}.
     * <p/>
     * All {@link #write(String, Object)} and {@link #read(String)} methods should be called after this
     * method is executed.
     *
     * @param context context, used to get application context
     */
    public static void init(Context context) {
        mContext = context.getApplicationContext();
        mPaperMap = new ConcurrentHashMap<>();
    }

    /**
     * This method will create new book paper instance for specific name
     *
     * @param name name of new database
     * @return Paper instance
     */
    public static Paper book(String name) {
        return book(name, true);
    }

    /**
     * This method will create new book paper instance for specific name
     *
     * @return Paper instance
     */
    public static Paper book() {
        return book(DEFAULT_DB_NAME, false);
    }

    private static Paper book(String name, boolean user) {
        if (mContext == null) throw new PaperDbException("Paper.init is not called");
        if (user && name.equals(DEFAULT_DB_NAME)) throw new PaperDbException(DEFAULT_DB_NAME +
                " name is reserved for default library name");
        Paper paper = mPaperMap.get(name);
        if (paper == null) {
            paper = new Paper(mContext, name);
            mPaperMap.put(name, paper);
        }
        return paper;
    }
    /**
     * Clears all data saved by Paper. Can be used even when Paper yet not initialized
     * by {@link #init(Context)}
     *
     * @param context context
     */
    public void clear(Context context) {
        mStorage.destroy();
    }

    /**
     * Saves any types of POJOs or collections in Paper storage.
     *
     * @param key   object key is used as part of object's file name
     * @param value object to save, must have no-arg constructor
     * @param <T>   object type
     * @return this Paper instance
     */
    public <T> Paper write(String key, T value) {
        if (value == null) {
            mStorage.deleteIfExists(key);
        } else {
            mStorage.insert(key, value);
        }
        return this;
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
    public <T> T read(String key) {
        return read(key, null);
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
    public <T> T read(String key, T defaultValue) {
        T value = mStorage.select(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Check if an object with the given key is saved in Paper storage.
     *
     * @param key object key
     * @return true if object with given key exists in Paper storage, false otherwise
     */
    public boolean exist(String key) {
        return mStorage.exist(key);
    }

    /**
     * Delete saved object for given key if it is exist.
     *
     * @param key object key
     * @return this Paper instance
     */
    public Paper delete(String key) {
        mStorage.deleteIfExists(key);
        return this;
    }

    private Paper(Context context) {
        this(context, DEFAULT_DB_NAME);
    }

    private Paper(Context context, String dbName) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName);
    }

}
