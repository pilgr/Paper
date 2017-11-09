package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class CustomBookTest {

    @Before
    public void setUp() {
        Paper.init(getTargetContext());
    }

    @Test
    public void getFolderPathForBook_custom() {
        String path = Paper.book("custom").getPath();
        assertTrue(path.endsWith("/io.paperdb.test/files/custom"));
    }

    @Test
    public void getFilePathForKey_customBook() {
        String path = Paper.book("custom").getPath("my_key");
        assertTrue(path.endsWith("/io.paperdb.test/files/custom/my_key.pt"));
    }

    @Test
    public void readWriteDeleteToDifferentBooks() {
        String custom = "custom";
        Paper.book().destroy();
        Paper.book(custom).destroy();

        Paper.book().write("city", "Victoria");
        Paper.book(custom).write("city", "Kyiv");

        assertEquals("Victoria", Paper.book().read("city"));
        assertEquals("Kyiv", Paper.book(custom).read("city"));

        Paper.book().delete("city");
        assertFalse(Paper.book().contains("city"));
        assertTrue(Paper.book(custom).contains("city"));
    }



}
