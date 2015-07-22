package io.paperdb;

public interface CachedStorage extends Storage {

    void invalidateCache();

    void invalidateCache(String key);
}
