package io.paperdb;

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;

import util.DelayMeasurer;

public class PaperDbSpeedTest extends AndroidTestCase {
    //Latest results
    //Nexus 5
//    09-04 23:59:08.262  19291-19333/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadList 197ms
//    09-04 23:59:39.994  19291-19333/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadWrite100Times/100 317ms
//    09-04 23:59:40.185  19291-19333/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testSaveList 174ms
    //Genymotion (mac charged)
//    09-05 00:00:58.944  10484-10497/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadList 78ms
//    09-05 00:01:10.792  10484-10497/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadWrite100Times/100 118ms
//    09-05 00:01:10.860  10484-10497/? D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testSaveList 56ms
    // With Kryo 3.0.1
//    06-02 20:51:12.128  15878-15896/io.paperdb.test D/DelayMeasurer﹕ testReadList 48ms
//    06-02 20:51:17.292  15878-15896/io.paperdb.test D/DelayMeasurer﹕ testReadWrite100Times/100 51ms
//    06-02 20:51:17.316  15878-15896/io.paperdb.test D/DelayMeasurer﹕ testSaveList 20ms
    private static final int LIST_SIZE = 1000;

    private static final String TEST_DB = "test-db";
    private static final String TEST_TABLE = "test-table";
    private PaperDb mPaperDb;

    public void setUp() throws Exception {
        PaperDb.destroy(getContext(), TEST_DB);
        mPaperDb = new PaperDb(getContext(), TEST_DB);
    }

    public void testSaveList() throws Exception {
        final List<Person> contacts = genPersonList(LIST_SIZE);
        DelayMeasurer.start("testSaveList");
        mPaperDb.insert(TEST_TABLE, contacts);
        DelayMeasurer.finish("testSaveList");
    }

    public void testReadList() throws Exception {
        final List<Person> contacts = genPersonList(LIST_SIZE);
        mPaperDb.insert(TEST_TABLE, contacts);

        DelayMeasurer.start("testReadList");
        final List<Person> readContacts = mPaperDb.select(TEST_TABLE);
        DelayMeasurer.finish("testReadList");
        assertEquals(contacts.size(), readContacts.size());
    }

    public void testReadWrite100Times100Items() throws Exception {
        final int repeat = 100;
        final List<Person> contacts = genPersonList(1000);
        List<Person> readContacts = null;
        DelayMeasurer.start("testReadWrite100Times");
        for (int i = 0; i < repeat; i++) {
            mPaperDb.insert(TEST_TABLE, contacts);
            readContacts = mPaperDb.select(TEST_TABLE);
        }
        assertEquals(contacts.size(), readContacts.size());
        DelayMeasurer.finish("testReadWrite100Times", repeat);
    }

    private List<Person> genPersonList(int size) {
        List<Person> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Person p = new Person();
            p.mAge = i;
            p.mBikes = new String[2];
            p.mBikes[0] = "Kellys";
            p.mBikes[1] = "Trek";
            p.mPhoneNumbers = new ArrayList<>();
            p.mPhoneNumbers.add("234092348" + i);
            p.mPhoneNumbers.add("+380-44-234234234" + i);
            list.add(p);
        }
        return list;
    }

}