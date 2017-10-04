package io.paperdb.multithread;

import android.support.annotation.NonNull;
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
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void read_write_same_key() throws InterruptedException {
        startWritingLargeDataSetInSeparateThread("dataset");

        Log.d(TAG, "read dataset: start");
        // Read for same key 'dataset' should be postponed until writing is done
        List<Person> readData = Paper.book().read("dataset", Collections.<Person>emptyList());
        assertEquals(10000, readData.size());
        Log.d(TAG, "read dataset: finish");
    }

    @Test
    public void write_exists_same_key() throws InterruptedException {
        assertFalse(Paper.book().contains("dataset"));

        startWritingLargeDataSetInSeparateThread("dataset");

        Log.d(TAG, "check dataset contains: start");
        // Read for same key 'dataset' should be postponed until writing is done
        assertTrue(Paper.book().contains("dataset"));
        Log.d(TAG, "check dataset contains: finish");
    }

    @Test
    public void write_delete_same_key() throws InterruptedException {
        assertFalse(Paper.book().contains("dataset"));

        startWritingLargeDataSetInSeparateThread("dataset");

        Log.d(TAG, "check dataset delete: start");
        // Read for same key 'dataset' should be postponed until writing is done
        Paper.book().delete("dataset");
        assertFalse(Paper.book().contains("dataset"));
        Log.d(TAG, "check dataset delete: finish");
    }

    @Test
    public void read_write_different_keys() throws InterruptedException {
        // Primary write something else
        Paper.book().write("city", "Victoria");

        // Start writing large dataset
        CountDownLatch writeEndLatch = startWritingLargeDataSetInSeparateThread("dataset");

        Log.d(TAG, "read other key: start");
        // Read for different key 'city' should be locked by writing other key 'dataset'
        assertEquals("Victoria", Paper.book().read("city"));
        assertEquals(1, writeEndLatch.getCount());
        writeEndLatch.await(5, TimeUnit.SECONDS);
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

    @NonNull
    private CountDownLatch startWritingLargeDataSetInSeparateThread(
            @SuppressWarnings("SameParameterValue") final String key) throws InterruptedException {
        final CountDownLatch writeStartLatch = new CountDownLatch(1);
        final CountDownLatch writeFinishLatch = new CountDownLatch(1);
        new Thread() {
            final List<Person> dataset = TestDataGenerator.genPersonList(10000);

            @Override
            public void run() {
                Log.d(TAG, "write '" + key + "': start");
                writeStartLatch.countDown();
                Paper.book().write(key, dataset);
                Log.d(TAG, "write '" + key + "': finish");
                writeFinishLatch.countDown();
            }
        }.start();
        writeStartLatch.await(5, TimeUnit.SECONDS);
        // A small delay is required to let writer thread start writing data and acquire a lock
        Thread.sleep(100);
        return writeFinishLatch;
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
