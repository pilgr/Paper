package io.paperdb;

import android.test.AndroidTestCase;

import java.util.Set;

import io.paperdb.testdata.Person;

import static io.paperdb.testdata.TestDataGenerator.getPersonSet;

/**
 * Tests save Sets
 */
public class SaveSetTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        Paper.destroy(getContext());
        Paper.init(getContext());
    }

    public void testDefaultSet() {
        assertEquals(0, Paper.getSet("non-existed").size());
    }

    public void testPutSet0() throws Exception {
        final Set<Person> inserted = getPersonSet(0);
        Paper.putSet("persons", inserted);

        Paper.init(getContext()); //Use new Paper instance
        final Set<Person> selected = Paper.getSet("persons");
        assertEquals(0, selected.size());
    }

    public void testPutSetNull() throws Exception {
        final Set<Person> inserted = getPersonSet(10);
        Paper.putSet("persons", inserted);
        Paper.init(getContext());
        final Set<Person> selected = Paper.getSet("persons");
        assertEquals(inserted, selected);

        Paper.putSet("persons", null);
        Set<Person> selectedNull = Paper.getSet("persons");
        assertEquals(0, selectedNull.size());
    }

    public void testPutSet10() {
        final Set<Person> inserted = getPersonSet(10);
        Paper.putSet("person set", inserted);

        Paper.init(getContext());
        final Set<Person> selected = Paper.getSet("person set");

        assertEquals(inserted, selected);
    }
}
