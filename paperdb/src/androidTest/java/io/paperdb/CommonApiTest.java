package io.paperdb;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.paperdb.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;

@RunWith(AndroidJUnit4.class)
public class CommonApiTest extends AndroidTestCase {

    @Before
    public void setUp() throws Exception {
        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
    }

    @Test
    public void testExist() throws Exception {
        assertFalse(Paper.exist("persons"));
        Paper.putList("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.exist("persons"));
    }

    @Test
    public void testDelete() throws Exception {
        Paper.putList("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.exist("persons"));
        Paper.delete("persons");
        assertFalse(Paper.exist("persons"));
    }

    @Test
    public void testDeleteNotExisted() throws Exception {
        assertFalse(Paper.exist("persons"));
        Paper.delete("persons");
    }

    @Test
    public void testDestroy() throws Exception {
        Paper.putList("persons", TestDataGenerator.genPersonList(10));
        Paper.putList("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Paper.exist("persons"));
        assertTrue(Paper.exist("persons2"));

        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
        assertFalse(Paper.exist("persons"));
        assertFalse(Paper.exist("persons2"));
    }

}