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

    private static Context mContext;

    private static ConcurrentHashMap<String, Book> mBookMap;

    /**
     * Lightweight method to init Paper instance. Should be executed in {@link Application#onCreate()}
     * or {@link android.app.Activity#onCreate(Bundle)}.
     * <p/>
     *
     * @param context context, used to get application context
     */
    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * This method will create new book paper instance for specific name
     *
     * @param name name of new database
     * @return Paper instance
     */
    public static Book book(String name) {
        if (name.equals(DEFAULT_DB_NAME)) throw new PaperDbException(DEFAULT_DB_NAME +
                " name is reserved for default library name");
        return getBook(name);
    }

    /**
     * This method will create new book paper instance for specific name
     *
     * @return Book instance
     */
    public static Book book() {
        return getBook(DEFAULT_DB_NAME);
    }

    private static Book getBook(String name) {
        if (mContext == null) {
            throw new PaperDbException("Paper.init is not called");
        }
        if (mBookMap == null) {
            mBookMap = new ConcurrentHashMap<>();
        }
        Book book = mBookMap.get(name);
        if (book == null) {
            book = new Book(mContext, name);
            mBookMap.put(name, book);
        }
        return book;
    }
}
