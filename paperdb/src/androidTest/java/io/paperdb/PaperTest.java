package io.paperdb;

import android.test.AndroidTestCase;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.TestDataGenerator;

public class PaperTest extends AndroidTestCase {
    private static final String TAG = "PaperTest";

    public void setUp() throws Exception {
        super.setUp();
        Paper.destroy(getContext());
        Paper.init(getContext());
    }

    public void testInsert0() throws Exception {
        final List<Person> inserted = TestDataGenerator.genPersonList(0);
        Paper.insert("persons", inserted);

        Paper.init(getContext()); //Use new Paper instance
        final List<Person> selected = Paper.select("persons");
        assertEquals(0, selected.size());
    }

    public void testInsertNull() throws Exception {
        final List<Person> inserted = TestDataGenerator.genPersonList(10);
        Paper.insert("persons", inserted);
        Paper.init(getContext());
        final List<Person> selected = Paper.select("persons");
        assertEquals(inserted, selected);

        Paper.insert("persons", null);
        List<Person> selectedNull = Paper.select("persons");
        assertEquals(0, selectedNull.size());
    }

    public void testInsert1() throws Exception {
        final List<Person> inserted = TestDataGenerator.genPersonList(1);
        Paper.insert("persons", inserted);

        Paper.init(getContext());
        final List<Person> selected = Paper.select("persons");

        assertEquals(inserted, selected);
    }

    public void testInsert10() {
        final List<Person> inserted = TestDataGenerator.genPersonList(10);
        Paper.insert("persons", inserted);

        Paper.init(getContext());
        final List<Person> selected = Paper.select("persons");

        assertEquals(inserted, selected);
    }

    public void testReplace() {
        final List<Person> inserted10 = TestDataGenerator.genPersonList(10);
        Paper.insert("persons", inserted10);
        Paper.init(getContext());
        final List<Person> selected10 = Paper.select("persons");
        assertEquals(inserted10, selected10);

        final List<Person> replace1 = TestDataGenerator.genPersonList(1);
        Paper.insert("persons", replace1);
        Paper.init(getContext());
        List<Person> selected1 = Paper.select("persons");
        assertEquals(replace1, selected1);
    }

    public void testSelect() {
        //Select from not existed
        final List<Person> selected = Paper.select("persons");
        assertEquals(0, selected.size());

        //Select from existed
        final List<Person> inserted100 = TestDataGenerator.genPersonList(100);
        Paper.insert("persons", inserted100);
        final List<Person> selected100 = Paper.select("persons");
        assertEquals(inserted100, selected100);
    }

    public void testExist() throws Exception {
        assertFalse(Paper.exist("persons"));
        Paper.insert("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.exist("persons"));
    }

    public void testDelete() throws Exception {
        Paper.insert("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.exist("persons"));
        Paper.delete("persons");
        assertFalse(Paper.exist("persons"));
    }

    public void testDeleteNotExisted() throws Exception {
        assertFalse(Paper.exist("persons"));
        Paper.delete("persons");
    }

    public void testDestroy() throws Exception {
        Paper.insert("persons", TestDataGenerator.genPersonList(10));
        Paper.insert("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Paper.exist("persons"));
        assertTrue(Paper.exist("persons2"));

        Paper.destroy(getContext());
        Paper.init(getContext());
        assertFalse(Paper.exist("persons"));
        assertFalse(Paper.exist("persons2"));
    }

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
            @Override public void run() {
                int size = new Random().nextInt(200);
                final List<Person> inserted100 = TestDataGenerator.genPersonList(size);
                Paper.insert("persons", inserted100);
                Log.d(TAG, "Inserted " + size);
            }
        };
    }

    private Runnable getSelectRunnable() {
        return new Runnable() {
            @Override public void run() {
                List<Person> selected = Paper.select("persons");
                Log.d(TAG, "Selected " + selected.size());
            }
        };
    }

}