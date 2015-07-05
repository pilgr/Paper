package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.paperdb.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class PaperTest {

    @Before
    public void setUp() throws Exception {
        Paper.init(getTargetContext());
        Paper.book().destroy();
    }

    @Test
    public void testExist() throws Exception {
        assertFalse(Paper.book().exist("persons"));
        Paper.book().write("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.book().exist("persons"));
    }

    @Test
    public void testDelete() throws Exception {
        Paper.book().write("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.book().exist("persons"));
        Paper.book().delete("persons");
        assertFalse(Paper.book().exist("persons"));
    }

    @Test
    public void testDeleteNotExisted() throws Exception {
        assertFalse(Paper.book().exist("persons"));
        Paper.book().delete("persons");
    }

    @Test
    public void testClear() throws Exception {
        Paper.book().write("persons", TestDataGenerator.genPersonList(10));
        Paper.book().write("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Paper.book().exist("persons"));
        assertTrue(Paper.book().exist("persons2"));

        Paper.book().destroy();
        // init() call is not required after clear()
        assertFalse(Paper.book().exist("persons"));
        assertFalse(Paper.book().exist("persons2"));

        // Should be possible to continue to use Paper after clear()
        Paper.book().write("persons3", TestDataGenerator.genPersonList(30));
        assertTrue(Paper.book().exist("persons3"));
        assertThat(Paper.book().<List>read("persons3")).hasSize(30);
    }

    @Test
    public void testPutGetNormal() {
        Paper.book().write("city", "Lund");
        String val = Paper.book().read("city", "default");
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testPutGetNormalAfterReinit() {
        Paper.book().write("city", "Lund");
        String val = Paper.book().read("city", "default");
        Paper.init(getTargetContext());// Reinit Paper instance
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testGetNotExisted() {
        String val = Paper.book().read("non-existed");
        assertThat(val).isNull();
    }

    @Test
    public void testGetDefault() {
        String val = Paper.book().read("non-existed", "default");
        assertThat(val).isEqualTo("default");
    }

    @Test
    public void testPutNull() {
        Paper.book().write("city", "Lund");
        String val = Paper.book().read("city");
        assertThat(val).isEqualTo("Lund");

        Paper.book().write("city", null);
        String nullVal = Paper.book().read("city");
        assertThat(nullVal).isNull();
    }

    @Test
    public void testReplace() {
        Paper.book().write("city", "Lund");
        assertThat(Paper.book().read("city")).isEqualTo("Lund");
        Paper.book().write("city", "Kyiv");
        assertThat(Paper.book().read("city")).isEqualTo("Kyiv");
    }

    @Test
    public void testValidKeyNames() {
        Paper.book().write("city", "Lund");
        assertThat(Paper.book().read("city")).isEqualTo("Lund");

        Paper.book().write("city.dasd&%", "Lund");
        assertThat(Paper.book().read("city.dasd&%")).isEqualTo("Lund");

        Paper.book().write("city-ads", "Lund");
        assertThat(Paper.book().read("city-ads")).isEqualTo("Lund");
    }

    @Test(expected=PaperDbException.class)
    public void testInvalidKeyNameBackslash() {
        Paper.book().write("city/ads", "Lund");
        assertThat(Paper.book().read("city/ads")).isEqualTo("Lund");
    }

    @Test(expected=PaperDbException.class)
    public void testBookname() {
        Paper.book().write("city", "Lund");
        assertThat(Paper.book().read("city")).isEqualTo("Lund");
        //TODO replace "io.paperdb" with the reflection which will take value from the class
        Paper.book("io.paperdb").write("city", "Lund");
    }
}