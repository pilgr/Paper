package io.paperdb.benchmark;

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.paperdb.Paper;
import io.paperdb.testdata.Person;
import io.paperdb.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class Benchmark extends AndroidTestCase {

    private static final String TAG = "paper-benchmark";

    @SuppressWarnings("FieldCanBeLocal")
    private static final int REPEAT_COUNT = 30;

    @Test
    public void testReadWrite500Contacts() throws Exception {
        final List<Person> contacts = TestDataGenerator.genPersonList(500);
        Paper.init(getTargetContext());
        Paper.book().destroy();
        long paperTime = runTest(new PaperReadWriteContactsTest(), contacts, REPEAT_COUNT);

        Hawk.init(getTargetContext());
        Hawk.clear();
        long hawkTime = runTest(new HawkReadWriteContactsTest(), contacts, REPEAT_COUNT);

        printResults("Read/write 500 contacts", paperTime, hawkTime);
    }

    @Test
    public void testWrite500Contacts() throws Exception {
        final List<Person> contacts = TestDataGenerator.genPersonList(500);
        Paper.init(getTargetContext());
        Paper.book().destroy();
        long paperTime = runTest(new PaperWriteContactsTest(), contacts, REPEAT_COUNT);

        Hawk.init(getTargetContext());
        Hawk.clear();
        long hawkTime = runTest(new HawkWriteContactsTest(), contacts, REPEAT_COUNT);

        printResults("Write 500 contacts", paperTime, hawkTime);
    }

    @Test
    public void testRead500Contacts() throws Exception {
        final List<Person> contacts = TestDataGenerator.genPersonList(500);
        Paper.init(getTargetContext());
        Paper.book().destroy();
        runTest(new PaperWriteContactsTest(), contacts, REPEAT_COUNT); //Prepare
        long paperTime = runTest(new PaperReadContactsTest(), contacts, REPEAT_COUNT);

        Hawk.init(getTargetContext());
        Hawk.clear();
        runTest(new HawkWriteContactsTest(), contacts, REPEAT_COUNT); //Prepare
        long hawkTime = runTest(new HawkReadContactsTest(), contacts, REPEAT_COUNT);

        printResults("Read 500 contacts", paperTime, hawkTime);
    }

    private void printResults(String name, long paperTime, long hawkTime) {
        Log.i(TAG, String.format("..................................\n%s \n Paper: %d \n Hawk: %d",
                name, paperTime, hawkTime));
    }

    private <T> long runTest(TestTask<T> task, T extra, int repeat) {
        long start = SystemClock.uptimeMillis();
        for (int i = 0; i < repeat; i++) {
            task.run(i, extra);
        }
        return (SystemClock.uptimeMillis() - start) / repeat;
    }

    interface TestTask<T> {
        void run(int i, T extra);
    }

    private class PaperReadWriteContactsTest implements TestTask<List<Person>> {
        @Override
        public void run(int i, List<Person> extra) {
            String key = "contacts" + i;
            Paper.book().write(key, extra);
            Paper.book().<List<Person>>read(key);
        }
    }

    private class HawkReadWriteContactsTest implements TestTask<List<Person>> {
        @Override
        public void run(int i, List<Person> extra) {
            String key = "contacts" + i;
            Hawk.put(key, extra);
            Hawk.<List<Person>>get(key);
        }
    }

    private class PaperWriteContactsTest implements TestTask<List<Person>> {
        @Override
        public void run(int i, List<Person> extra) {
            String key = "contacts" + i;
            Paper.book().write(key, extra);
        }
    }

    private class HawkWriteContactsTest implements TestTask<List<Person>> {
        @Override
        public void run(int i, List<Person> extra) {
            String key = "contacts" + i;
            Hawk.put(key, extra);
        }
    }

    private class PaperReadContactsTest implements TestTask<List<Person>> {
        @Override
        public void run(int i, List<Person> extra) {
            String key = "contacts" + i;
            Paper.book().<List<Person>>read(key);
        }
    }

    private class HawkReadContactsTest implements TestTask<List<Person>> {
        @Override
        public void run(int i, List<Person> extra) {
            String key = "contacts" + i;
            Hawk.<List<Person>>get(key);
        }
    }
}
