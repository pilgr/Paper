package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.paperdb.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class PaperTest {

    @Before
    public void setUp() throws Exception {
        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
    }

    @Test
    public void testExist() throws Exception {
        assertFalse(Paper.exist("persons"));
        Paper.put("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.exist("persons"));
    }

    @Test
    public void testDelete() throws Exception {
        Paper.put("persons", TestDataGenerator.genPersonList(10));
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
        Paper.put("persons", TestDataGenerator.genPersonList(10));
        Paper.put("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Paper.exist("persons"));
        assertTrue(Paper.exist("persons2"));

        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
        assertFalse(Paper.exist("persons"));
        assertFalse(Paper.exist("persons2"));
    }

    @Test
    public void testPutGetNormal() {
        Paper.put("city", "Lund");
        String val = Paper.get("city", "default");
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testPutGetNormalAfterReinit() {
        Paper.put("city", "Lund");
        String val = Paper.get("city", "default");
        Paper.init(getTargetContext());// Reinit Paper instance
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testGetNotExisted() {
        String val = Paper.get("non-existed");
        assertThat(val).isNull();
    }

    @Test
    public void testGetDefault() {
        String val = Paper.get("non-existed", "default");
        assertThat(val).isEqualTo("default");
    }

    @Test
    public void testPutNull() {
        Paper.put("city", "Lund");
        String val = Paper.get("city");
        assertThat(val).isEqualTo("Lund");

        Paper.put("city", null);
        String nullVal = Paper.get("city");
        assertThat(nullVal).isNull();
    }

    @Test
    public void testReplace() {
        Paper.put("city", "Lund");
        Paper.put("city", "Kyiv");
        assertThat(Paper.get("city")).isEqualTo("Kyiv");
    }
}