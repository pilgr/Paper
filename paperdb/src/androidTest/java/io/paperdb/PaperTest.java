package io.paperdb;

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import de.javakaffee.kryoserializers.jodatime.JodaDateTimeSerializer;
import io.paperdb.testdata.TestDataGenerator;
import io.paperdb.utils.TestUtils;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class PaperTest {

    @Before
    public void setUp() throws Exception {
        Paper.init(getTargetContext());
        Paper.book().destroy();
    }

    @Test
    public void testContains() throws Exception {
        assertFalse(Paper.book().contains("persons"));
        Paper.book().write("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.book().contains("persons"));
    }

    @Test
    public void testDelete() throws Exception {
        Paper.book().write("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Paper.book().contains("persons"));
        Paper.book().delete("persons");
        assertFalse(Paper.book().contains("persons"));
    }

    @Test
    public void testDeleteNotExisted() throws Exception {
        assertFalse(Paper.book().contains("persons"));
        Paper.book().delete("persons");
    }

    @Test
    public void testClear() throws Exception {
        Paper.book().write("persons", TestDataGenerator.genPersonList(10));
        Paper.book().write("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Paper.book().contains("persons"));
        assertTrue(Paper.book().contains("persons2"));

        Paper.book().destroy();
        // init() call is not required after clear()
        assertFalse(Paper.book().contains("persons"));
        assertFalse(Paper.book().contains("persons2"));

        // Should be possible to continue to use Paper after clear()
        Paper.book().write("persons3", TestDataGenerator.genPersonList(30));
        assertTrue(Paper.book().contains("persons3"));
        assertThat(Paper.book().<List>read("persons3")).hasSize(30);
    }

    @Test
    public void testWriteReadNormal() {
        Paper.book().write("city", "Lund");
        String val = Paper.book().read("city", "default");
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testWriteReadNormalAfterReinit() {
        Paper.book().write("city", "Lund");
        String val = Paper.book().read("city", "default");
        Paper.init(getTargetContext());// Reinit Paper instance
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testReadNotExisted() {
        String val = Paper.book().read("non-existed");
        assertThat(val).isNull();
    }

    @Test
    public void testReadDefault() {
        String val = Paper.book().read("non-existed", "default");
        assertThat(val).isEqualTo("default");
    }

    @Test(expected = PaperDbException.class)
    public void testWriteNull() {
        Paper.book().write("city", null);
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

    @Test(expected = PaperDbException.class)
    public void testInvalidKeyNameBackslash() {
        Paper.book().write("city/ads", "Lund");
        assertThat(Paper.book().read("city/ads")).isEqualTo("Lund");
    }

    @Test(expected = PaperDbException.class)
    public void testGetBookWithDefaultBookName() {
        Paper.book(Paper.DEFAULT_DB_NAME);
    }

    @Test
    public void testCustomBookReadWrite() {
        final String NATIVE = "native";
        assertThat(Paper.book()).isNotSameAs(Paper.book(NATIVE));
        Paper.book(NATIVE).destroy();

        Paper.book().write("city", "Lund");
        Paper.book(NATIVE).write("city", "Kyiv");

        assertThat(Paper.book().read("city")).isEqualTo("Lund");
        assertThat(Paper.book(NATIVE).read("city")).isEqualTo("Kyiv");
    }

    @Test
    public void testCustomBookDestroy() {
        final String NATIVE = "native";
        Paper.book(NATIVE).destroy();

        Paper.book().write("city", "Lund");
        Paper.book(NATIVE).write("city", "Kyiv");

        Paper.book(NATIVE).destroy();

        assertThat(Paper.book().read("city")).isEqualTo("Lund");
        assertThat(Paper.book(NATIVE).read("city")).isNull();
    }

    @Test
    public void testGetAllKeys() {
        Paper.book().destroy();

        Paper.book().write("city", "Lund");
        Paper.book().write("city1", "Lund1");
        Paper.book().write("city2", "Lund2");
        List<String> allKeys = Paper.book().getAllKeys();

        assertThat(allKeys.size()).isEqualTo(3);
        assertThat(allKeys.contains("city")).isTrue();
        assertThat(allKeys.contains("city1")).isTrue();
        assertThat(allKeys.contains("city2")).isTrue();
    }

    @Test
    public void testCustomSerializer() {
        Paper.addSerializer(DateTime.class, new JodaDateTimeSerializer());
        DateTime now = DateTime.now(DateTimeZone.UTC);

        Paper.book().write("joda-datetime", now);
        assertEquals(now, Paper.book().read("joda-datetime"));
    }

    @Test
    public void testTimestampNoObject() {
        Paper.book().destroy();
        long timestamp = Paper.book().lastModified("city");
        assertEquals(-1, timestamp);
    }

    @Test
    public void testTimestamp() {
        long testStartMS = System.currentTimeMillis();

        Paper.book().destroy();
        Paper.book().write("city", "Lund");

        long fileWriteMS = Paper.book().lastModified("city");
        assertNotEquals(-1, fileWriteMS);

        long elapsed = fileWriteMS - testStartMS;
        // Many file systems only support seconds granularity for last-modification time
        assertThat(elapsed < 1000 || elapsed > -1000).isTrue();
    }

    @Test
    public void testTimestampChanges() {
        Paper.book().destroy();
        Paper.book().write("city", "Lund");
        long fileWrite1MS = Paper.book().lastModified("city");

        // Add 1 sec delay as many file systems only support seconds granularity for last-modification time
        SystemClock.sleep(1000);

        Paper.book().write("city", "Kyiv");
        long fileWrite2MS = Paper.book().lastModified("city");

        assertThat(fileWrite2MS > fileWrite1MS).isTrue();
    }

    @Test
    public void testDbFileExistsAfterFailedRead() throws IOException {
        String key = "cityMap";
        assertFalse(Paper.book().contains(key));

        TestUtils.replacePaperDbFileBy("invalid_data.pt", key);
        assertTrue(Paper.book().contains(key));

        Throwable expectedException = null;
        try {
            Paper.book().read(key);
        } catch (PaperDbException e) {
            expectedException = e;
        }
        assertNotNull(expectedException);
        // Data file should exist even if previous read attempt was failed
        assertTrue(Paper.book().contains(key));
    }

    @Test
    public void getFolderPathForBook_default() {
        String path = Paper.book().getPath();
        assertTrue(path.endsWith("/io.paperdb.test/files/io.paperdb"));
    }

    @Test
    public void getFilePathForKey_defaultBook() {
        String path = Paper.book().getPath("my_key");
        assertTrue(path.endsWith("/io.paperdb.test/files/io.paperdb/my_key.pt"));
    }

}