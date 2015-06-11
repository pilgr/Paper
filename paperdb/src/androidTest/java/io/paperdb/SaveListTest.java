package io.paperdb;

import android.test.AndroidTestCase;

import java.util.List;

import io.paperdb.testdata.Person;

import static io.paperdb.testdata.TestDataGenerator.genPersonList;

/**
 * Tests save Lists
 */
public class SaveListTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        Paper.destroy(getContext());
        Paper.init(getContext());
    }

    public void testDefaultList() {
        assertEquals(0, Paper.getList("non-existed").size());
    }

    public void testPutList0() throws Exception {
        final List<Person> inserted = genPersonList(0);
        Paper.putList("persons", inserted);

        Paper.init(getContext()); //Use new Paper instance
        final List<Person> selected = Paper.getList("persons");
        assertEquals(0, selected.size());
    }

    public void testPutListNull() throws Exception {
        final List<Person> inserted = genPersonList(10);
        Paper.putList("persons", inserted);
        Paper.init(getContext());
        final List<Person> selected = Paper.getList("persons");
        assertEquals(inserted, selected);

        Paper.putList("persons", null);
        List<Person> selectedNull = Paper.getList("persons");
        assertEquals(0, selectedNull.size());
    }

    public void testPutList1() throws Exception {
        final List<Person> inserted = genPersonList(1);
        Paper.putList("persons", inserted);

        Paper.init(getContext());
        final List<Person> selected = Paper.getList("persons");

        assertEquals(inserted, selected);
    }

    public void testPutList10() {
        final List<Person> inserted = genPersonList(10);
        Paper.putList("persons", inserted);

        Paper.init(getContext());
        final List<Person> selected = Paper.getList("persons");

        assertEquals(inserted, selected);
    }

    public void testReplace() {
        final List<Person> inserted10 = genPersonList(10);
        Paper.putList("persons", inserted10);
        Paper.init(getContext());
        final List<Person> selected10 = Paper.getList("persons");
        assertEquals(inserted10, selected10);

        final List<Person> replace1 = genPersonList(1);
        Paper.putList("persons", replace1);
        Paper.init(getContext());
        List<Person> selected1 = Paper.getList("persons");
        assertEquals(replace1, selected1);
    }

    public void testGet() {
        //Select from not existed
        final List<Person> selected = Paper.getList("persons");
        assertEquals(0, selected.size());

        //Select from existed
        final List<Person> inserted100 = genPersonList(100);
        Paper.putList("persons", inserted100);
        final List<Person> selected100 = Paper.getList("persons");
        assertEquals(inserted100, selected100);
    }

}
