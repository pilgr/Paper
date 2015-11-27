package io.paperdb;

import java.util.List;

interface Storage {

    void destroy();

    <E> void insert(String key, E value);

    <E> E select(String key);

    boolean exist(String key);

    void deleteIfExists(String key);

    List<String> getAllKeys();
}
