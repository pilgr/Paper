package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

import io.paperdb.testdata.Person;
import io.paperdb.testdata.TestDataGenerator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

import io.paperdb.DbStoragePlainFile;
import io.paperdb.Paper;

@RunWith(AndroidJUnit4.class)
public class StorageTest {

    private DbStoragePlainFile storage;
    private static final File FILES_DIRECTORY = new File("storage.dir");

    @Before
    public void setUp() throws Exception {
        storage = new DbStoragePlainFile(getTargetContext().getFilesDir(), Paper.DEFAULT_DB_NAME);
    }

    @Test
    public void insertTest() {
        Person person = TestDataGenerator.genPerson(0);
        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(person,retrievedPerson);
    }

    @Test
    public void testInsertWithRelation() {
        Person person = TestDataGenerator.genPerson(1);
        Person relatedPerson = TestDataGenerator.genPerson(2);
        person.setRelative(relatedPerson);
        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(person, retrievedPerson);
        assertEquals(relatedPerson,retrievedPerson.getRelative());
    }

    @Test
    public void testEqualObjectsWithNotEqualReferences() {
        Person person1 = TestDataGenerator.genPerson(1);
        Person person2 = TestDataGenerator.genPerson(1);
        assertEquals(person1, person2);
        assertTrue(person1 != person2);
        storage.insert("testPerson1", person1);
        storage.insert("testPerson2", person2);
        Person retrievedPerson1 = storage.select("testPerson1");
        Person retrievedPerson2 = storage.select("testPerson2");
        assertEquals(retrievedPerson1, retrievedPerson2);
        assertTrue(retrievedPerson1 != retrievedPerson2);
    }

    @Test
    public void testInsertWithSelfReference() {
        Person person = TestDataGenerator.genPerson(1);
        person.setRelative(person);
        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(retrievedPerson, retrievedPerson.getRelative());
    }

    @Test
    public void testInsertWithCyclicReference() {
        Person person = TestDataGenerator.genPerson(1);
        Person relatedPerson = TestDataGenerator.genPerson(2);
        person.setRelative(relatedPerson);
        relatedPerson.setRelative(relatedPerson);

        storage.insert("testPerson", person);
        Person retrievedPerson = storage.select("testPerson");
        assertEquals(retrievedPerson.getRelative(), retrievedPerson.getRelative().getRelative());
    }

    @After
    public void clear() throws Exception {
        storage.destroy();
    }
}
