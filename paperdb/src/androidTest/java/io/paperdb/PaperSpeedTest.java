package io.paperdb;

import android.test.AndroidTestCase;

import java.util.List;

import util.DelayMeasurer;
import util.TestDataGenerator;

public class PaperSpeedTest extends AndroidTestCase {
    //Latest results
    //Nexus 5
//    09-04 23:59:08.262  19291-19333/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadList 197ms
//    09-04 23:59:39.994  19291-19333/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadWrite100Times/100 317ms
//    09-04 23:59:40.185  19291-19333/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testSaveList 174ms
    //Genymotion (mac charged)
//    09-05 00:00:58.944  10484-10497/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadList 78ms
//    09-05 00:01:10.792  10484-10497/name.pilgr.appdialer D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testReadWrite100Times/100 118ms
//    09-05 00:01:10.860  10484-10497/? D/AppDialer﹕ [DelayMeasurer:checkTime:42]: testSaveList 56ms
    // With Kryo 3.0.1 //Genymotion (mac battery)
//    06-02 20:51:12.128  15878-15896/io.paperdb.test D/DelayMeasurer﹕ testReadList 48ms
//    06-02 20:51:17.292  15878-15896/io.paperdb.test D/DelayMeasurer﹕ testReadWrite100Times/100 51ms
//    06-02 20:51:17.316  15878-15896/io.paperdb.test D/DelayMeasurer﹕ testSaveList 20ms
    //Compatible serializer //Genymotion (mac battery)
//    06-02 21:40:31.729  23701-23719/? D/DelayMeasurer﹕ testReadList 29ms
//    06-02 21:40:36.592  23701-23719/? D/DelayMeasurer﹕ testReadWrite100Times/100 48ms
//    06-02 21:40:36.617  23701-23719/? D/DelayMeasurer﹕ testSaveList 21ms
    //VersionFieldSerializer //Genymotion (mac battery)
//    06-02 21:59:07.132    7637-7661/io.paperdb.test D/DelayMeasurer﹕ testReadList 30ms
//    06-02 21:59:10.960    7637-7661/io.paperdb.test D/DelayMeasurer﹕ testReadWrite100Times/100 38ms
//    06-02 21:59:10.980    7637-7661/io.paperdb.test D/DelayMeasurer﹕ testSaveList 15ms
    //VersionFieldSerializer //Genymotion (mac battery) + private fields
//    22:17:15.985  23352-23370/io.paperdb.test D/DelayMeasurer﹕ testReadList 25ms
//    06-02 22:17:19.965  23352-23370/io.paperdb.test D/DelayMeasurer﹕ testReadWrite100Times/100 39ms
//    06-02 22:17:19.989  23352-23370/io.paperdb.test D/DelayMeasurer﹕ testSaveList 18ms
    //VersionFieldSerializer //Nexus 4 Android 5.1 + private fields
//    06-03 00:22:37.036    2152-2170/io.paperdb.test D/DelayMeasurer﹕ testReadList 153ms
//    06-03 00:23:08.254    2152-2170/io.paperdb.test D/DelayMeasurer﹕ testReadWrite100Times/100 312ms
//    06-03 00:23:08.465    2152-2170/io.paperdb.test D/DelayMeasurer﹕ testSaveList 197ms

    private static final int LIST_SIZE = 1000;

    private static final String TEST_TABLE = "test-table";

    public void setUp() throws Exception {
        Paper.destroy(getContext());
        Paper.init(getContext());
    }

    public void testSaveList() throws Exception {
        final List<Person> contacts = TestDataGenerator.genPersonList(LIST_SIZE);
        DelayMeasurer.start("testSaveList");
        Paper.insert(TEST_TABLE, contacts);
        DelayMeasurer.finish("testSaveList");
    }

    public void testReadList() throws Exception {
        final List<Person> contacts = TestDataGenerator.genPersonList(LIST_SIZE);
        Paper.insert(TEST_TABLE, contacts);

        DelayMeasurer.start("testReadList");
        final List<Person> readContacts = Paper.select(TEST_TABLE);
        DelayMeasurer.finish("testReadList");
        assertEquals(contacts.size(), readContacts.size());
    }

    public void testReadWrite100Times100Items() throws Exception {
        final int repeat = 100;
        final List<Person> contacts = TestDataGenerator.genPersonList(1000);
        List<Person> readContacts = null;
        DelayMeasurer.start("testReadWrite100Times");
        for (int i = 0; i < repeat; i++) {
            Paper.insert(TEST_TABLE, contacts);
            readContacts = Paper.select(TEST_TABLE);
        }
        assertEquals(contacts.size(), readContacts.size());
        DelayMeasurer.finish("testReadWrite100Times", repeat);
    }

}