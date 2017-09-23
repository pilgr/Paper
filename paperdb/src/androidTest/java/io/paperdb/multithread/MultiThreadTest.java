package io.paperdb.multithread;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import io.paperdb.testdata.Person;
import io.paperdb.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;

/**
 * Tests read/write into Paper data from multiple threads
 */
@RunWith(AndroidJUnit4.class)
public class MultiThreadTest {

    private static final String TAG = "MultiThreadTest";

    @Before
    public void setUp() {
        Paper.init(getTargetContext());
        Paper.book().destroy();
    }

    @Test
    public void synchronizedBySameKey() throws InterruptedException {
        final List<Person> dataset = TestDataGenerator.genPersonList(10000);
        final CountDownLatch allTasksLatch = new CountDownLatch(2);
        final CountDownLatch writeStartLatch = new CountDownLatch(1);

        final Thread readThread = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "readRunnable: start");
                // Read for same key 'dataset' should be postponed until writing is done
                List<Person> readData = Paper.book().read("dataset", Collections.<Person>emptyList());
                assertEquals(10000, readData.size());
                Log.d(TAG, "readRunnable: finish");
                allTasksLatch.countDown();
            }
        };

        Thread writeThread = new Thread() {
            @Override
            public void run() {
                readThread.start();

                writeStartLatch.countDown();
                Log.d(TAG, "writeRunnable: start");
                Paper.book().write("dataset", dataset);
                Log.d(TAG, "writeRunnable: finish");
                allTasksLatch.countDown();
            }
        };

        writeThread.start();
        allTasksLatch.await(10000, TimeUnit.SECONDS);
    }

    @Test
    public void notSynchronizedByDifferentKeys() throws InterruptedException {
        final List<Person> dataset = TestDataGenerator.genPersonList(10000);
        final CountDownLatch writeStartLatch = new CountDownLatch(1);
        final CountDownLatch writeEndLatch = new CountDownLatch(1);

        // Primary write something else
        Paper.book().write("city", "Victoria");
        // Start writing large dataset
        new Thread() {
            @Override
            public void run() {
                writeStartLatch.countDown();
                Log.d(TAG, "write dataset: start");
                Paper.book().write("dataset", dataset);
                Log.d(TAG, "write dataset: finish");
                writeEndLatch.countDown();
            }
        }.start();
        writeStartLatch.await(10000, TimeUnit.SECONDS);

        Log.d(TAG, "read other key: start");
        // Read for different key 'city' should be locked by writing other key 'dataset'
        assertEquals("Victoria", Paper.book().read("city"));
        assertEquals(1, writeEndLatch.getCount());
        writeEndLatch.await();
        assertEquals(10000, Paper.book().<List<Person>>read("dataset").size());
        Log.d(TAG, "read other key: finish");
    }

    @Test
    public void testMultiThreadAccess() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Object>> todo = new LinkedList<>();

        for (int i = 0; i <= 1000; i++) {
            Runnable task;
            if (i % 2 == 0) {
                task = getInsertRunnable();
            } else {
                task = getSelectRunnable();
            }
            todo.add(Executors.callable(task));
        }
        List<Future<Object>> futures = executor.invokeAll(todo);
        for (Future<Object> future : futures) {
            future.get();
        }
    }

    private Runnable getInsertRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                int size = new Random().nextInt(200);
                final List<Person> inserted100 = TestDataGenerator.genPersonList(size);
                Paper.book().write("persons", inserted100);
            }
        };
    }

    private Runnable getSelectRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                Paper.book().read("persons");
            }
        };
    }
}
