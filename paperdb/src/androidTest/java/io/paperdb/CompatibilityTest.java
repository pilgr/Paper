package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests support for forward and backward compatibility. Fields can be added or removed.
 * Changing the type of a field is not supported except very limited cases like int->long.
 **/
@RunWith(AndroidJUnit4.class)
public class CompatibilityTest {

    @Before
    public void setUp() throws Exception {
        Paper.clear(getTargetContext());
        Paper.init(getTargetContext());
    }

    @Test
    public void testChangeClass() throws IllegalAccessException, NoSuchFieldException, InstantiationException {
        TestClass tc = getClassInstanceWithNewName(TestClass.class, TestClassNew.class.getName());
        tc.name = "original";
        tc.value = "test";
        tc.timestamp = 123;

        // Save original class. Only class name is changed to TestClassNew
        Paper.put("test", tc);

        // Read and instantiate a modified class TestClassNew based on saved data in TestClass
        TestClassNew tc_new = Paper.get("test");
        // Check original value is restored despite new default value in TestClassNew
        assertThat(tc_new.name).isEqualTo("original");
        // Check default value for new added field
        assertThat(tc_new.newField).isEqualTo("default");
        // Check compatible field type change
        assertThat(tc_new.timestamp).isEqualTo(123l);
    }

    private <T> T getClassInstanceWithNewName(Class<T> classToInstantiate, String newName)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Field name = classToInstantiate.getClass().getDeclaredField("name");
        name.setAccessible(true);
        name.set(classToInstantiate, newName);
        return classToInstantiate.newInstance();
    }

    public static class TestClass {
        public String name = "original";
        public String value = "test";
        public int timestamp;
    }

    /**
     * Emulates changes in class TestClass
     */
    public static class TestClassNew {
        public String name = "new-class";
        // Has been removed
        // public String value;
        public String newField = "default";
        public long timestamp;
    }

}
