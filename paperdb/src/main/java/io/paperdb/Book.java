package io.paperdb;

import android.content.Context;

import java.util.List;

public class Book {

    private final Storage mStorage;

    protected Book(Context context, String dbName) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName);
    }

    /**
     * Destroys all data saved in Book.
     */
    public void destroy() {
        mStorage.destroy();
    }

    /**
     * Saves any types of POJOs or collections in Book storage.
     *
     * @param key   object key is used as part of object's file name
     * @param value object to save, must have no-arg constructor, can't be null.
     * @param <T>   object type
     * @return this Book instance
     */
    public <T> Book write(String key, T value) {
        if (value == null) {
            throw new PaperDbException("Paper doesn't support writing null root values");
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
     * Check if an object with the given key is saved in Book storage.
     *
     * @param key object key
     * @return true if object with given key exists in Book storage, false otherwise
     */
    public boolean exist(String key) {
        return mStorage.exist(key);
    }

    /**
     * Delete saved object for given key if it is exist.
     *
     * @param key object key
     */
    public void delete(String key) {
        mStorage.deleteIfExists(key);
    }

    /**
     * Returns all keys for objects in book.
     *
     * @return all keys
     */
    public List<String> getAllKeys() {
        return mStorage.getAllKeys();
    }

}
