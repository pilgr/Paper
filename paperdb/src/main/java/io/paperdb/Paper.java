package io.paperdb;

import android.content.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static <T> void putList(String key, List<T> list) {
        putCollection(key, list);
    }

    public static <T> void putSet(String key, Set<T> set) {
        putCollection(key, set);
    }

    public static <K, V> void putMap(String key, Map<K, V> map) {
        if (map == null || map.size() == 0) {
            INSTANCE.mStorage.deleteIfExists(key);
            return;
        }
        INSTANCE.mStorage.insert(key, map);
    }

    public static <T> List<T> getList(String key) {
        List<T> list = INSTANCE.mStorage.select(key, null);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    public static <T> Set<T> getSet(String key) {
        Set<T> set = INSTANCE.mStorage.select(key, null);
        if (set == null) {
            set = Collections.emptySet();
        }
        return set;
    }

    public static <K, V> Map<K, V> getMap(String key) {
        Map<K, V> map = INSTANCE.mStorage.select(key, null);
        if (map == null) {
            map = Collections.emptyMap();
        }
        return map;
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

    private static <T> void putCollection(String key, Collection<T> items) {
        if (items == null || items.size() == 0) {
            INSTANCE.mStorage.deleteIfExists(key);
            return;
        }
        INSTANCE.mStorage.insert(key, items);
    }

}
