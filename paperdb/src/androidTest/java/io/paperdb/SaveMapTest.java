package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import io.paperdb.testdata.Person;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static io.paperdb.testdata.TestDataGenerator.genPersonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests save and restore Maps
 */
@RunWith(AndroidJUnit4.class)
public class SaveMapTest {

    @Before
    public void setUp() throws Exception {
        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
    }

    @Test
    public void testDefaultMap() {
        assertThat(Paper.getMap("non-existed")).isEmpty();
    }

    @Test
    public void testPutMap0() throws Exception {
        final Map<Integer, Person> inserted = genPersonMap(0);
        Paper.putMap("person-map", inserted);

        Paper.init(getTargetContext()); //Use new Paper instance
        assertThat(Paper.getMap("person-map")).isEmpty();
    }

    @Test
    public void testPutMapNull() throws Exception {
        final Map<Integer, Person> inserted = genPersonMap(10);
        Paper.putMap("person-map", inserted);
        Paper.init(getTargetContext());
        assertThat(Paper.getMap("person-map")).isEqualTo(inserted);

        Paper.putMap("person-map", null);
        assertThat(Paper.getMap("person-map")).isEmpty();
    }

    @Test
    public void testPutMap1() throws Exception {
        final Map<Integer, Person> inserted = genPersonMap(1);
        Paper.putMap("person-map", inserted);

        Paper.init(getTargetContext());
        assertThat(Paper.getMap("person-map")).isEqualTo(inserted);
    }

    @Test
    public void testPutMap10() {
        final Map<Integer, Person> inserted = genPersonMap(10);
        Paper.putMap("person-map", inserted);

        Paper.init(getTargetContext());
        assertThat(Paper.getMap("person-map")).isEqualTo(inserted);
    }

    @Test
    public void testReplace() {
        final Map<Integer, Person> inserted10 = genPersonMap(10);
        Paper.putMap("person-map", inserted10);
        Paper.init(getTargetContext());
        assertThat(Paper.getMap("person-map")).isEqualTo(inserted10);

        final Map<Integer, Person> replace1 = genPersonMap(1);
        Paper.putMap("person-map", replace1);
        Paper.init(getTargetContext());
        assertThat(Paper.getMap("person-map")).isEqualTo(replace1);

    }

    @Test
    public void testGet() {
        //Select from not existed
        assertThat(Paper.getMap("person-map")).isEmpty();

        //Select from existed
        final Map<Integer, Person> inserted100 = genPersonMap(100);
        Paper.putMap("person-map", inserted100);
        assertThat(Paper.getMap("person-map")).isEqualTo(inserted100);
    }

}
