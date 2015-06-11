package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.paperdb.testdata.Person;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static io.paperdb.testdata.TestDataGenerator.genPersonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests save Lists
 */
@RunWith(AndroidJUnit4.class)
public class SaveListTest {

    @Before
    public void setUp() throws Exception {
        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
    }

    @Test
    public void testDefaultList() {
        assertThat(Paper.getList("non-existed")).isEmpty();
    }

    @Test
    public void testPutList0() throws Exception {
        final List<Person> inserted = genPersonList(0);
        Paper.putList("persons", inserted);

        Paper.init(getTargetContext()); //Use new Paper instance
        assertThat(Paper.getList("persons")).isEmpty();
    }

    @Test
    public void testPutListNull() throws Exception {
        final List<Person> inserted = genPersonList(10);
        Paper.putList("persons", inserted);
        Paper.init(getTargetContext());
        assertThat(Paper.getList("persons")).isEqualTo(inserted);

        Paper.putList("persons", null);
        assertThat(Paper.getList("persons")).isEmpty();
    }

    @Test
    public void testPutList1() throws Exception {
        final List<Person> inserted = genPersonList(1);
        Paper.putList("persons", inserted);

        Paper.init(getTargetContext());
        assertThat(Paper.getList("persons")).isEqualTo(inserted);
    }

    @Test
    public void testPutList10() {
        final List<Person> inserted = genPersonList(10);
        Paper.putList("persons", inserted);

        Paper.init(getTargetContext());
        assertThat(Paper.getList("persons")).isEqualTo(inserted);
    }

    @Test
    public void testReplace() {
        final List<Person> inserted10 = genPersonList(10);
        Paper.putList("persons", inserted10);
        Paper.init(getTargetContext());
        assertThat(Paper.getList("persons")).isEqualTo(inserted10);

        final List<Person> replace1 = genPersonList(1);
        Paper.putList("persons", replace1);
        Paper.init(getTargetContext());
        assertThat(Paper.getList("persons")).isEqualTo(replace1);

    }

    @Test
    public void testGet() {
        //Select from not existed
        assertThat(Paper.getList("persons")).isEmpty();

        //Select from existed
        final List<Person> inserted100 = genPersonList(100);
        Paper.putList("persons", inserted100);
        assertThat(Paper.getList("persons")).isEqualTo(inserted100);
    }

}
