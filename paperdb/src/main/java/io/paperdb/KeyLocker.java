package io.paperdb;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

/**
 * This class allows multiple threads to lock against a string key
 * <p>
 * Created by hiperion on 2017/3/15.
 */
class KeyLocker {
    private ConcurrentMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    // Global semaphore is required to block global operations involving _all_ keys
    // like destroying the paper folder or getting all keys
    private Semaphore global = new Semaphore(1, true);

    void acquire(String key) {
        int availablePermits = global.availablePermits();

        // If global semaphore is acquired, wait until global operation is done
        if (availablePermits == 0) {
            global.acquireUninterruptibly();
            global.release();
        }

        if (key == null) {
            throw new IllegalArgumentException("Key couldn't be null");
        }

        if (!semaphoreMap.containsKey(key)) {
            semaphoreMap.put(key, new Semaphore(1, true));
        }

        Semaphore semaphore = semaphoreMap.get(key);
        semaphore.acquireUninterruptibly();
    }

    void release(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key couldn't be null");
        }

        Semaphore semaphore = semaphoreMap.get(key);
        if (semaphore == null) {
            throw new IllegalStateException("Couldn't release semaphore. The acquire() with the same key '"
                    + key + "' has to be called prior to calling release()");
        }
        semaphore.release();
    }

    void acquireGlobal() {
        // Set global block
        global.acquireUninterruptibly();
        // And wait for other keys to be released
        for (Semaphore semaphore : semaphoreMap.values()) {
            semaphore.acquireUninterruptibly();
        }
    }

    void releaseGlobal() {
        for (Semaphore semaphore : semaphoreMap.values()) {
            semaphore.release();
        }
        global.release();
    }
}
