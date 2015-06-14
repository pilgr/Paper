package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import io.paperdb.testdata.Person;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static io.paperdb.testdata.TestDataGenerator.genPerson;
import static io.paperdb.testdata.TestDataGenerator.genPersonList;
import static io.paperdb.testdata.TestDataGenerator.genPersonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests List put/get API
 */
@RunWith(AndroidJUnit4.class)
public class DataTest {

    @Before
    public void setUp() throws Exception {
        Paper.clear(getTargetContext());
        Paper.init(getTargetContext());
    }

    @Test
    public void testPutEmptyList() throws Exception {
        final List<Person> inserted = genPersonList(0);
        Paper.put("persons", inserted);
        assertThat((List) Paper.get("persons")).isEmpty();
    }

    @Test
    public void testPutGetList() {
        final List<Person> inserted = genPersonList(10000);
        Paper.put("persons", inserted);
        List<Person> persons = Paper.get("persons");
        assertThat(persons).isEqualTo(inserted);
    }

    @Test
    public void testPutMap() {
        final Map<Integer, Person> inserted = genPersonMap(10000);
        Paper.put("persons", inserted);

        final Map<Integer, Person> personMap = Paper.get("persons");
        assertThat(personMap).isEqualTo(inserted);
    }

    @Test
    public void testPutPOJO() {
        final Person person = genPerson(1);
        Paper.put("profile", person);

        final Person savedPerson = Paper.get("profile");
        assertThat(savedPerson).isEqualTo(person);
        assertThat(savedPerson).isNotSameAs(person);
    }

}
