package io.paperdb;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

/**
 * This class allows multiple threads to lock against a string key
 *
 * Created by hiperion on 2017/3/15.
 */
public class KeyLocker {
    private ConcurrentMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<String, Semaphore>();

    public KeyLocker(){}

    public void acquire(String key) throws Exception {
        if( key == null )
            throw new NullPointerException();
        Semaphore semaphore = new Semaphore(1, true);
        Semaphore semaphoreExist = semaphoreMap.putIfAbsent(key, semaphore);
        if(semaphoreExist == null){
            semaphoreExist = semaphore;
        }
        semaphoreExist.acquireUninterruptibly();
    }

    public void release(String key)  throws Exception{
        if( key == null )
            throw new NullPointerException();
        Semaphore semaphore = semaphoreMap.get(key);
        if(semaphore != null){
            semaphore.release();
        }
    }
}
