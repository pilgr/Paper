package io.paperdb;

import com.esotericsoftware.kryo.Serializer;

interface Storage {

    void destroy();

    <E> void insert(String key, E value);

    <E> E select(String key);

    boolean exist(String key);

    void deleteIfExists(String key);

    <T> void registerSerializer(Class<T> type, Serializer<T> serializer);
}
