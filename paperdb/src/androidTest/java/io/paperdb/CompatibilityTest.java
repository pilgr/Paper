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
        Paper.init(getTargetContext());
        Paper.book().destroy();
    }

    @Test
    public void testChangeClass()
            throws IllegalAccessException, NoSuchFieldException, InstantiationException {
        TestClass testClass
                = getClassInstanceWithNewName(TestClass.class, TestClassNew.class.getName());
        testClass.name = "original";
        testClass.value = "test";
        testClass.timestamp = 123;

        // Save original class. Only class name is changed to TestClassNew
        Paper.book().write("test", testClass);

        // Read and instantiate a modified class TestClassNew based on saved data in TestClass
        TestClassNew newTestClass = Paper.book().read("test");
        // Check original value is restored despite new default value in TestClassNew
        assertThat(newTestClass.name).isEqualTo("original");
        // Check default value for new added field
        assertThat(newTestClass.newField).isEqualTo("default");
        // Check compatible field type change
        assertThat(newTestClass.timestamp).isEqualTo(123l);
    }

    @Test(expected = PaperDbException.class)
    public void testNotCompatibleClassChanges() throws Exception {
        TestClass testClass = getClassInstanceWithNewName(TestClass.class,
                TestClassNotCompatible.class.getName());
        testClass.timestamp = 123;
        Paper.book().write("not-compatible", testClass);

        Paper.book().<TestClassNotCompatible>read("not-compatible");
    }

    @Test
    public void testTransientFields() throws Exception {
        TestClassTransient tc = new TestClassTransient();
        tc.timestamp = 123;
        tc.transientField = "changed";

        Paper.book().write("transient-class", tc);

        TestClassTransient readTc = Paper.book().read("transient-class");
        assertThat(readTc.timestamp).isEqualTo(123);
        assertThat(readTc.transientField).isEqualTo("default");
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

    /**
     * Emulates not compatible changes in class TestClass
     */
    public static class TestClassNotCompatible {
        public String name = "not-compatible-class";
        public String timestamp; //Changed field type long->String
    }

    public static class TestClassTransient {
        public String name = "transient";
        public transient String transientField = "default";
        public int timestamp;
    }


}
