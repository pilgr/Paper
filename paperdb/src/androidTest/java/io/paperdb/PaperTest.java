package io.paperdb;

import android.test.AndroidTestCase;

import java.util.List;

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
        Paper.putList("persons", inserted);

        Paper.init(getContext()); //Use new Paper instance
        final List<Person> selected = Paper.getList("persons");
        assertEquals(0, selected.size());
    }

    public void testInsertNull() throws Exception {
        final List<Person> inserted = TestDataGenerator.genPersonList(10);
        Paper.putList("persons", inserted);
        Paper.init(getContext());
        final List<Person> selected = Paper.getList("persons");
        assertEquals(inserted, selected);

        Paper.putList("persons", null);
        List<Person> selectedNull = Paper.getList("persons");
        assertEquals(0, selectedNull.size());
    }

    public void testInsert1() throws Exception {
        final List<Person> inserted = TestDataGenerator.genPersonList(1);
        Paper.putList("persons", inserted);

        Paper.init(getContext());
        final List<Person> selected = Paper.getList("persons");

        assertEquals(inserted, selected);
    }

    public void testInsert10() {
        final List<Person> inserted = TestDataGenerator.genPersonList(10);
        Paper.putList("persons", inserted);

        Paper.init(getContext());
        final List<Person> selected = Paper.getList("persons");

        assertEquals(inserted, selected);
    }

    public void testReplace() {
        final List<Person> inserted10 = TestDataGenerator.genPersonList(10);
        Paper.putList("persons", inserted10);
        Paper.init(getContext());
        final List<Person> selected10 = Paper.getList("persons");
        assertEquals(inserted10, selected10);

        final List<Person> replace1 = TestDataGenerator.genPersonList(1);
        Paper.putList("persons", replace1);
        Paper.init(getContext());
        List<Person> selected1 = Paper.getList("persons");
        assertEquals(replace1, selected1);
    }

    public void testSelect() {
        //Select from not existed
        final List<Person> selected = Paper.getList("persons");
        assertEquals(0, selected.size());

        //Select from existed
        final List<Person> inserted100 = TestDataGenerator.genPersonList(100);
        Paper.putList("persons", inserted100);
        final List<Person> selected100 = Paper.getList("persons");
        assertEquals(inserted100, selected100);
    }

    public void testExist() throws Exception {
        assertFalse(Paper.exist("persons"));
        Paper.putList("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.exist("persons"));
    }

    public void testDelete() throws Exception {
        Paper.putList("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.exist("persons"));
        Paper.delete("persons");
        assertFalse(Paper.exist("persons"));
    }

    public void testDeleteNotExisted() throws Exception {
        assertFalse(Paper.exist("persons"));
        Paper.delete("persons");
    }

    public void testDestroy() throws Exception {
        Paper.putList("persons", TestDataGenerator.genPersonList(10));
        Paper.putList("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Paper.exist("persons"));
        assertTrue(Paper.exist("persons2"));

        Paper.destroy(getContext());
        Paper.init(getContext());
        assertFalse(Paper.exist("persons"));
        assertFalse(Paper.exist("persons2"));
    }

}