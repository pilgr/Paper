package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

@RunWith(AndroidJUnit4.class)
public class CustomBookLocationTest {

    @Before
    public void setUp() {
        Paper.init(getTargetContext());
    }

    @Test
    public void readWriteDelete_customLocation_defaultBook() {
        String customLocation = getTargetContext().getFilesDir() + "/custom/location";
        Book bookOnSdcard = Paper.bookOn(customLocation);
        Book defaultBook = Paper.book();

        bookOnSdcard.destroy();
        defaultBook.destroy();

        bookOnSdcard.write("city", "Victoria");
        defaultBook.write("city", "Kyiv");

        assertEquals("Victoria", bookOnSdcard.read("city"));
        assertEquals("Kyiv", defaultBook.read("city"));

        bookOnSdcard.delete("city");

        assertFalse(bookOnSdcard.exists("city"));
        assertEquals("Kyiv", defaultBook.read("city"));
    }

    @Test
    public void readWriteDelete_customLocation_customBook() {
        String customLocation = getTargetContext().getFilesDir() + "/custom/location";
        Book bookOnSdcard = Paper.bookOn(customLocation, "encyclopedia");
        Book defaultBook = Paper.book("encyclopedia");

        bookOnSdcard.destroy();
        defaultBook.destroy();

        bookOnSdcard.write("city", "Victoria");
        defaultBook.write("city", "Kyiv");

        assertEquals("Victoria", bookOnSdcard.read("city"));
        assertEquals("Kyiv", defaultBook.read("city"));

        bookOnSdcard.delete("city");

        assertFalse(bookOnSdcard.exists("city"));
        assertEquals("Kyiv", defaultBook.read("city"));
    }

    @Test
    public void getPath() {
        Book defaultBookOnSdCard = Paper.bookOn("/sdcard");
        Book encyclopediaOnSdCard = Paper.bookOn("/sdcard", "encyclopedia");

        assertEquals("/sdcard/io.paperdb", defaultBookOnSdCard.getPath());
        assertEquals("/sdcard/io.paperdb/key.pt", defaultBookOnSdCard.getPath("key"));
        assertEquals("/sdcard/encyclopedia", encyclopediaOnSdCard.getPath());
        assertEquals("/sdcard/encyclopedia/key.pt", encyclopediaOnSdCard.getPath("key"));
    }

    @Test
    public void bookInstanceIsTheSameForSameLocationAndBookName() {
        Book defaultBook = Paper.book();
        Book encyclopedia = Paper.book("encyclopedia");
        Book defaultBookOnSdCard = Paper.bookOn("/sdcard");
        Book encyclopediaOnSdCard = Paper.bookOn("/sdcard", "encyclopedia");

        // Check all instances are unique
        HashSet<Book> instanceSet = new HashSet<>();
        instanceSet.add(defaultBook);
        instanceSet.add(encyclopedia);
        instanceSet.add(defaultBookOnSdCard);
        instanceSet.add(encyclopediaOnSdCard);
        assertEquals(4, instanceSet.size());

        assertSame(defaultBook, Paper.book());
        assertSame(encyclopedia, Paper.book("encyclopedia"));
        assertSame(defaultBookOnSdCard, Paper.bookOn("/sdcard"));
        assertSame(encyclopediaOnSdCard, Paper.bookOn("/sdcard", "encyclopedia"));
    }

    @Test
    public void locationCanBeWithFileSeparatorAtTheEnd() {
        assertEquals("/sdcard/io.paperdb", Paper.bookOn("/sdcard").getPath());
        assertEquals("/sdcard/io.paperdb", Paper.bookOn("/sdcard/").getPath());
        assertEquals("/sdcard/encyclopedia",
                Paper.bookOn("/sdcard", "encyclopedia").getPath());
        assertEquals("/sdcard/encyclopedia",
                Paper.bookOn("/sdcard/", "encyclopedia").getPath());
    }
}
