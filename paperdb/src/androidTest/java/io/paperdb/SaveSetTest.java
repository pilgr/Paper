package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import io.paperdb.testdata.Person;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static io.paperdb.testdata.TestDataGenerator.genPersonSet;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests save and restore Sets
 */
@RunWith(AndroidJUnit4.class)
public class SaveSetTest {

    @Before
    public void setUp() throws Exception {
        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
    }

    @Test
    public void testDefaultSet() {
        assertThat(Paper.getSet("non-existed")).isEmpty();
    }

    @Test
    public void testPutSet0() throws Exception {
        final Set<Person> inserted = genPersonSet(0);
        Paper.putSet("persons", inserted);

        Paper.init(getTargetContext()); //Use new Paper instance
        assertThat(Paper.getSet("persons")).isEmpty();
    }

    @Test
    public void testPutSetNull() throws Exception {
        final Set<Person> inserted = genPersonSet(10);
        Paper.putSet("persons", inserted);

        Paper.init(getTargetContext());
        assertThat(Paper.getSet("persons")).isEqualTo(inserted);

        Paper.putSet("persons", null);
        assertThat(Paper.getSet("persons")).isEmpty();
    }

    @Test
    public void testPutSet10() {
        final Set<Person> inserted = genPersonSet(10);
        Paper.putSet("person set", inserted);

        Paper.init(getTargetContext());
        assertThat(Paper.getSet("person set")).isEqualTo(inserted);
    }
}
