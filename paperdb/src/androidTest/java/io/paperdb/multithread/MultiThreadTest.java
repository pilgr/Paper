package io.paperdb.multithread;

import android.test.AndroidTestCase;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.paperdb.Paper;
import io.paperdb.testdata.Person;
import io.paperdb.testdata.TestDataGenerator;

/**
 * Tests read/write into Paper data from multiple threads
 */
public class MultiThreadTest extends AndroidTestCase {
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
