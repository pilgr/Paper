package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.paperdb.testdata.ClassWithoutPublicNoArgConstructor;
import io.paperdb.testdata.Person;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static io.paperdb.testdata.TestDataGenerator.genPerson;
import static io.paperdb.testdata.TestDataGenerator.genPersonList;
import static io.paperdb.testdata.TestDataGenerator.genPersonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests List write/read API
 */
@RunWith(AndroidJUnit4.class)
public class DataTest {

    @Before
    public void setUp() throws Exception {
        Paper.init(getTargetContext());
        Paper.book().destroy();
    }

    @Test
    public void testPutEmptyList() throws Exception {
        final List<Person> inserted = genPersonList(0);
        Paper.book().write("persons", inserted);
        assertThat(Paper.book().<List>read("persons")).isEmpty();
    }

    @Test
    public void testPutGetList() {
        final List<Person> inserted = genPersonList(10000);
        Paper.book().write("persons", inserted);
        List<Person> persons = Paper.book().read("persons");
        assertThat(persons).isEqualTo(inserted);
    }

    @Test
    public void testPutMap() {
        final Map<Integer, Person> inserted = genPersonMap(10000);
        Paper.book().write("persons", inserted);

        final Map<Integer, Person> personMap = Paper.book().read("persons");
        assertThat(personMap).isEqualTo(inserted);
    }

    @Test
    public void testPutPOJO() {
        final Person person = genPerson(1);
        Paper.book().write("profile", person);

        final Person savedPerson = Paper.book().read("profile");
        assertThat(savedPerson).isEqualTo(person);
        assertThat(savedPerson).isNotSameAs(person);
    }

    @Test
    public void testPutSubAbstractListRandomAccess() {
        final List<Person> origin = genPersonList(100);
        List<Person> sublist = origin.subList(10, 30);
        testReadWriteWithoutClassCheck(sublist);
    }

    @Test
    public void testPutSubAbstractList() {
        final LinkedList<Person> origin = new LinkedList<>(genPersonList(100));
        List<Person> sublist = origin.subList(10, 30);
        testReadWriteWithoutClassCheck(sublist);
    }

    @Test
    public void testPutLinkedList() {
        final LinkedList<Person> origin = new LinkedList<>(genPersonList(100));
        testReadWrite(origin);
    }

    @Test
    public void testPutArraysAsLists() {
        testReadWrite(Arrays.asList("123", "345"));
    }

    @Test
    public void testPutCollectionsEmptyList() {
        testReadWrite(Collections.emptyList());
    }

    @Test
    public void testPutCollectionsEmptyMap() {
        testReadWrite(Collections.emptyMap());
    }

    @Test
    public void testPutCollectionsEmptySet() {
        testReadWrite(Collections.emptySet());
    }

    @Test
    public void testPutSingletonList() {
        testReadWrite(Collections.singletonList("item"));
    }

    @Test
    public void testPutSingletonSet() {
        testReadWrite(Collections.singleton("item"));
    }

    @Test
    public void testPutSingletonMap() {
        testReadWrite(Collections.singletonMap("key", "value"));
    }

    @Test
    public void testPutGeorgianCalendar() {
        testReadWrite(new GregorianCalendar());
    }

    @Test
    public void testPutSynchronizedList() {
        testReadWrite(Collections.synchronizedList(new ArrayList<>()));
    }

    @Test(expected = PaperDbException.class)
    public void testReadWriteClassWithoutNoArgConstructor() {
        testReadWrite(new ClassWithoutPublicNoArgConstructor("constructor argument"));
    }

    private Object testReadWriteWithoutClassCheck(Object originObj) {
        Paper.book().write("obj", originObj);
        Object readObj = Paper.book().read("obj");
        assertThat(readObj).isEqualTo(originObj);
        return readObj;
    }

    private void testReadWrite(Object originObj) {
        Object readObj = testReadWriteWithoutClassCheck(originObj);
        assertThat(readObj.getClass()).isEqualTo(originObj.getClass());
    }

}
