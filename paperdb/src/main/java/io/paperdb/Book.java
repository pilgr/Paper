package io.paperdb;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.esotericsoftware.kryo.Serializer;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class Book {

    private final DbStoragePlainFile mStorage;

    protected Book(Context context, String dbName, HashMap<Class, Serializer> serializers) {
        mStorage = new DbStoragePlainFile(context.getApplicationContext(), dbName, serializers);
    }

    protected Book(String dbPath, String dbName, HashMap<Class, Serializer> serializers) {
        mStorage = new DbStoragePlainFile(dbPath, dbName, serializers);
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
    public @NonNull <T> Book write(@NonNull String key, @NonNull T value) {
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
    public @Nullable <T> T read(@NonNull String key) {
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
    public @Nullable <T> T read(@NonNull String key, @Nullable T defaultValue) {
        T value = mStorage.select(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Checks if an object with the given key is saved in Book storage.
     *
     * @param key object key
     * @return true if Book storage contains an object with given key, false otherwise
     */
    public boolean contains(@NonNull String key) {
        return mStorage.exists(key);
    }

    /**
     * Checks if an object with the given key is saved in Book storage.
     *
     * @param key object key
     * @return true if object with given key exists in Book storage, false otherwise
     * @deprecated As of release 2.6, replaced by {@link #contains(String)}}
     */
    public boolean exist(@NonNull String key) {
        return mStorage.exists(key);
    }

    /**
     * Returns lastModified timestamp of last write in ms.
     * NOTE: only granularity in seconds is guaranteed. Some file systems keep
     * file modification time only in seconds.
     *
     * @param key object key
     * @return timestamp of last write for given key in ms if it exists, otherwise -1
     */
    public long lastModified(@NonNull String key) {
        return mStorage.lastModified(key);
    }

    /**
     * Delete saved object for given key if it is exist.
     *
     * @param key object key
     */
    public void delete(@NonNull String key) {
        mStorage.deleteIfExists(key);
    }

    /**
     * Returns all keys for objects in book.
     *
     * @return all keys
     */
    public @NonNull List<String> getAllKeys() {
        return mStorage.getAllKeys();
    }

    /**
     * Sets log level for internal Kryo serializer
     *
     * @param level one of levels from {@link com.esotericsoftware.minlog.Log }
     */
    public void setLogLevel(int level) {
        mStorage.setLogLevel(level);
    }

    /**
     * Returns path to a folder containing *.pt files for all keys kept
     * in the current Book. Could be handy for Book export/import purposes.
     * The returned path does not exist if the method has been called prior
     * saving any data in the current Book.
     * <p>
     * See also {@link #getPath(String)}.
     *
     * @return path to a folder locating data files for the current Book
     */
    public @NonNull String getPath() {
        return mStorage.getRootFolderPath();
    }

    /**
     * Returns path to a *.pt file containing saved object for a given key.
     * Could be handy for object export/import purposes.
     * The returned path does not exist if the method has been called prior
     * saving data for the given key.
     * <p>
     * See also {@link #getPath()}.
     *
     * @param key object key
     * @return path to a *.pt file containing saved object for a given key.
     */
    public @NonNull String getPath(@NonNull String key) {
        return mStorage.getOriginalFilePath(key);
    }
}
