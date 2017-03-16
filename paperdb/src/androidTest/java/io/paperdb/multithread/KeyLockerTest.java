package io.paperdb.multithread;

import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.Before;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.paperdb.Paper;
import io.paperdb.testdata.Person;
import io.paperdb.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Tests keylocker when read and write from multiple threads
 */
public class KeyLockerTest extends AndroidTestCase {
    int count = 4;//count of Runnable need to run.
    //The bean of person is too simply, so set large size.
    List<Person> insertedPersons = TestDataGenerator.genPersonList(100000);
    @Before
    public void setUp() throws Exception {
        Paper.clear(getTargetContext());
        Paper.init(getTargetContext());
    }

    public void testKeyLocker() throws Exception {
        ExecutorService mExecutor = Executors.newFixedThreadPool(10);
        mExecutor.submit(getInsertPersons());
        mExecutor.submit(getSelectPersons());
        mExecutor.submit(getInsertKey1());
        mExecutor.submit(getSelectKey1());

        while (count>0){
            //wait for test finish
        }
        Log.d(">>>>>>>","Finish");
    }

    private Runnable getInsertPersons() {
        return new Runnable() {
            @Override
            public void run() {
                long s1 = System.currentTimeMillis();
                Log.d(">>>>>>>", "write persons start");
                Paper.book().write("persons", insertedPersons);
                long s2 = System.currentTimeMillis();
                Log.d(">>>>>>>", "write persons end spend:" + (s2 - s1) + "ms");
                count--;
            }
        };
    }
    private Runnable getSelectPersons() {
        return new Runnable() {
            @Override
            public void run() {
                long s1 = System.currentTimeMillis();
                Log.d(">>>>>>>", "read persons start");
                Paper.book().read("persons");
                long s2 = System.currentTimeMillis();
                Log.d(">>>>>>>", "read persons end spend:" + (s2 - s1) + "ms");
                count--;
            }
        };
    }
    private Runnable getInsertKey1() {
        return new Runnable() {
            @Override
            public void run() {
                long s1 = System.currentTimeMillis();
                Log.d(">>>>>>>", "write key1 start");
                Paper.book().write("key1", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                long s2 = System.currentTimeMillis();
                Log.d(">>>>>>>", "write key1 end spend:" + (s2 - s1) + "ms");
                count--;
            }
        };
    }
    private Runnable getSelectKey1() {
        return new Runnable() {
            @Override
            public void run() {
                long s1 = System.currentTimeMillis();
                Log.d(">>>>>>>", "read key1 start");
                String result = Paper.book().read("key1");
                long s2 = System.currentTimeMillis();
                Log.d(">>>>>>>", "read key1 end spend:" + (s2 - s1) + "ms result:"+result);
                count--;
            }
        };
    }

}
