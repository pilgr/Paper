package io.paperdb;

import android.test.AndroidTestCase;

import io.paperdb.testdata.TestDataGenerator;

public class CommonApiTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        Paper.destroy(getContext());
        Paper.init(getContext());
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