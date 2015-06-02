package io.paperdb;

import android.test.AndroidTestCase;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PaperDbTest extends AndroidTestCase {
    private static final String TAG = "PaperDbTest";

    private static final String TEST_DB = "test-db";
    private PaperDb mPaperDb;

    public void setUp() throws Exception {
        super.setUp();
        PaperDb.destroy(getContext(), TEST_DB);
        mPaperDb = new PaperDb(getContext(), TEST_DB);
    }

    public void testInsert0() throws Exception {
        final List<Person> inserted = genPersonList(0);
        mPaperDb.insert("persons", inserted);

        PaperDb newDbInstance = new PaperDb(getContext(), TEST_DB);
        final List<Person> selected = newDbInstance.select("persons");
        assertEquals(0, selected.size());
    }

    public void testInsertNull() throws Exception {
        final List<Person> inserted = genPersonList(10);
        mPaperDb.insert("persons", inserted);
        PaperDb newDbInstance = new PaperDb(getContext(), TEST_DB);
        final List<Person> selected = newDbInstance.select("persons");
        assertEquals(inserted, selected);

        mPaperDb.insert("persons", null);
        List<Person> selectedNull = newDbInstance.select("persons");
        assertEquals(0, selectedNull.size());
    }

    public void testInsert1() throws Exception {
        final List<Person> inserted = genPersonList(1);
        mPaperDb.insert("persons", inserted);

        PaperDb newDbInstance = new PaperDb(getContext(), TEST_DB);
        final List<Person> selected = newDbInstance.select("persons");

        assertEquals(inserted, selected);
    }

    public void testInsert10() {
        final List<Person> inserted = genPersonList(10);
        mPaperDb.insert("persons", inserted);

        PaperDb newDbInstance = new PaperDb(getContext(), TEST_DB);
        final List<Person> selected = newDbInstance.select("persons");

        assertEquals(inserted, selected);
    }

    public void testReplace() {
        final List<Person> inserted10 = genPersonList(10);
        mPaperDb.insert("persons", inserted10);
        mPaperDb = new PaperDb(getContext(), TEST_DB);
        final List<Person> selected10 = mPaperDb.select("persons");
        assertEquals(inserted10, selected10);

        final List<Person> replace1 = genPersonList(1);
        mPaperDb.insert("persons", replace1);
        mPaperDb = new PaperDb(getContext(), TEST_DB);
        List<Person> selected1 = mPaperDb.select("persons");
        assertEquals(replace1, selected1);
    }

    public void testSelect() {
        //Select from not existed
        final List<Person> selected = mPaperDb.select("persons");
        assertEquals(0, selected.size());

        //Select from existed
        final List<Person> inserted100 = genPersonList(100);
        mPaperDb.insert("persons", inserted100);
        final List<Person> selected100 = mPaperDb.select("persons");
        assertEquals(inserted100, selected100);
    }

    public void testExist() throws Exception {
        assertFalse(mPaperDb.exist("persons"));
        mPaperDb.insert("persons", genPersonList(10));
        assertTrue(mPaperDb.exist("persons"));
    }

    public void testDelete() throws Exception {
        mPaperDb.insert("persons", genPersonList(10));
        assertTrue(mPaperDb.exist("persons"));
        mPaperDb.delete("persons");
        assertFalse(mPaperDb.exist("persons"));
    }

    public void testDeleteNotExisted() throws Exception {
        assertFalse(mPaperDb.exist("persons"));
        mPaperDb.delete("persons");
    }

    public void testDestroy() throws Exception {
        mPaperDb.insert("persons", genPersonList(10));
        mPaperDb.insert("persons2", genPersonList(20));
        assertTrue(mPaperDb.exist("persons"));
        assertTrue(mPaperDb.exist("persons2"));

        PaperDb.destroy(getContext(), TEST_DB);
        mPaperDb = new PaperDb(getContext(), TEST_DB);
        assertFalse(mPaperDb.exist("persons"));
        assertFalse(mPaperDb.exist("persons2"));
    }

    private List<Person> genPersonList(int size) {
        List<Person> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Person p = new Person();
            p.mAge = i;
            p.mBikes = new String[2];
            p.mBikes[0] = "Kellys";
            p.mBikes[1] = "Trek";
            p.mPhoneNumbers = new ArrayList<>();
            p.mPhoneNumbers.add("234092348" + i);
            p.mPhoneNumbers.add("+380-44-234234234" + i);
            list.add(p);
        }
        return list;
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
                final List<Person> inserted100 = genPersonList(size);
                mPaperDb.insert("persons", inserted100);
                Log.d(TAG, "Inserted " + size);
            }
        };
    }

    private Runnable getSelectRunnable() {
        return new Runnable() {
            @Override public void run() {
                List<Person> selected = mPaperDb.select("persons");
                Log.d(TAG, "Selected " + selected.size());
            }
        };
    }

}