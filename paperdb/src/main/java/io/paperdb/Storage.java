package io.paperdb;

import java.util.List;

interface Storage {

    void destroy();

    <E> void insert(String key, E value);

    <E> E select(String key);

    boolean exists(String key);

    long lastModified(String key);

    void deleteIfExists(String key);

    List<String> getAllKeys();

    void setLogLevel(int level);
}
