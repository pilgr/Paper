package io.paperdb;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.esotericsoftware.kryo.Serializer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Paper {
    static final String TAG = "paperdb";

    static final String DEFAULT_DB_NAME = "io.paperdb";

    // Keep _application_ context
    @SuppressLint("StaticFieldLeak") private static Context mContext;

    private static final ConcurrentHashMap<String, Book> mBookMap = new ConcurrentHashMap<>();
    private static final HashMap<Class, Serializer> mCustomSerializers = new HashMap<>();

    /**
     * Lightweight method to init Paper instance. Should be executed in {@link Application#onCreate()}
     * or {@link android.app.Activity#onCreate(Bundle)}.
     * <p/>
     *
     * @param context context, used to get application context
     */
    public static void init(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Alternative to {@link #init(Context)} when {@link #book()} method is not needed.
     *
     * <p><b><font color="red">
     *    Do not use this constructor unless you are explicitely specifying location by using {@link #bookOn(String)}
     *    or {@link #bookOn(String, String)}.
     * </font>
     */
    public static void init() {
    }

    /**
     * Returns book instance with the given name
     *
     * @param name name of new database
     * @return Paper instance
     */
    public static @NonNull Book book(@NonNull String name) {
        if (name.equals(DEFAULT_DB_NAME)) throw new PaperDbException(DEFAULT_DB_NAME +
                " name is reserved for default library name");
        return getBook(null, name);
    }

    /**
     * Returns default book instance
     *
     * @return Book instance
     */
    public static @NonNull Book book() {
        return getBook(null, DEFAULT_DB_NAME);
    }

    /**
     * Returns book instance to save data at custom location, e.g. on sdcard.
     *
     * @param location the path to a folder where the book's folder will be placed
     * @param name     the name of the book
     * @return book instance
     */
    public static @NonNull Book bookOn(@NonNull String location, @NonNull String name) {
        location = removeLastFileSeparatorIfExists(location);
        return getBook(location, name);
    }

    /**
     * Returns book instance to save data at custom location, e.g. on sdcard.
     *
     * @param location the path to a folder where the book's folder will be placed
     * @return book instance
     */
    public static @NonNull Book bookOn(@NonNull String location) {
        return bookOn(location, DEFAULT_DB_NAME);
    }

    private static Book getBook(String location, String name) {
        if (location == null && mContext == null) {
            throw new PaperDbException("Paper.init(Context) is not called. If you are calling Paper.init(), " +
                    "please call Paper.init(Context) instead.");
        }
        String key = (location == null ? "" : location) + name;
        synchronized (mBookMap) {
            Book book = mBookMap.get(key);
            if (book == null) {
                if (location == null) {
                    book = new Book(mContext, name, mCustomSerializers);
                } else {
                    book = new Book(location, name, mCustomSerializers);
                }
                mBookMap.put(key, book);
            }
            return book;
        }
    }

    private static String removeLastFileSeparatorIfExists(String customLocation) {
        if (customLocation.endsWith(File.separator)) {
            customLocation = customLocation.substring(0, customLocation.length() - 1);
        }
        return customLocation;
    }

    /**
     * @deprecated use Paper.book().write()
     */
    public static @NonNull <T> Book put(@NonNull String key, @NonNull T value) {
        return book().write(key, value);
    }

    /**
     * @deprecated use Paper.book().read()
     */
    public static @Nullable <T> T get(@NonNull String key) {
        return book().read(key);
    }

    /**
     * @deprecated use Paper.book().read()
     */
    public static @Nullable <T> T get(@NonNull String key, @Nullable T defaultValue) {
        return book().read(key, defaultValue);
    }

    /**
     * @deprecated use Paper.book().contains()
     */
    public static boolean exist(@NonNull String key) {
        return book().contains(key);
    }

    /**
     * @deprecated use Paper.book().delete()
     */
    public static void delete(@NonNull String key) {
        book().delete(key);
    }

    /**
     * @deprecated use Paper.book().destroy(). NOTE: Paper.init() be called
     * before destroy()
     */
    public static void clear(@NonNull Context context) {
        init(context);
        book().destroy();
    }

    /**
     * Sets log level for internal Kryo serializer
     *
     * @param level one of levels from {@link com.esotericsoftware.minlog.Log }
     */
    public static void setLogLevel(int level) {
        for (Map.Entry<String, Book> entry : mBookMap.entrySet()) {
            entry.getValue().setLogLevel(level);
        }
    }

    /**
     * Adds a custom serializer for a specific class
     * When used, must be called right after Paper.init()
     *
     * @param clazz      type of the custom serializer
     * @param serializer the serializer instance
     * @param <T>        type of the serializer
     */
    public static <T> void addSerializer(@NonNull Class<T> clazz, @NonNull Serializer<T> serializer) {
        if (!mCustomSerializers.containsKey(clazz))
            mCustomSerializers.put(clazz, serializer);
    }
}
