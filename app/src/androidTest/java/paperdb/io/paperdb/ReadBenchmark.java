package paperdb.io.paperdb;

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import paperdb.io.paperdb.testdata.Person;
import paperdb.io.paperdb.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ReadBenchmark {

    private static final String TAG = "Benchmark";

    @Before
    public void setUp() {
        Paper.init(getTargetContext());
        Paper.book().destroy();
    }

    @Test
    public void readSmallList() throws InterruptedException {
        List<Person> list = TestDataGenerator.genPersonList(1000);
        Paper.book().write("list1", list);
        Paper.book().write("list2", list);
        Paper.book().write("list3", list);

        long average = runManyTimes(10, new Runnable() {
            @Override
            public void run() {
                ExecutorService executor = Executors.newFixedThreadPool(3);
                executor.execute(readRunnable("list1"));
                executor.execute(readRunnable("list2"));
                executor.execute(readRunnable("list3"));
                executor.shutdown();
                boolean finished = false;
                try {
                    finished = executor.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                }
                assertTrue(finished);
            }
        });


        Log.d(TAG, "read 3 small lists in 3 threads: " + average + "ms");
    }

    @Test
    public void readLargeList() throws InterruptedException {
        List<Person> list = TestDataGenerator.genPersonList(50000);
        Paper.book().write("list1", list);
        Paper.book().write("list2", list);
        Paper.book().write("list3", list);

        long average = runManyTimes(3, new Runnable() {
            @Override
            public void run() {
                ExecutorService executor = Executors.newFixedThreadPool(3);
                executor.execute(readRunnable("list1"));
                executor.execute(readRunnable("list2"));
                executor.execute(readRunnable("list3"));
                executor.shutdown();
                boolean finished = false;
                try {
                    finished = executor.awaitTermination(40, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                }
                assertTrue(finished);
            }
        });


        Log.d(TAG, "read 3 large lists in 3 threads: " + average + "ms");
    }

    private Runnable readRunnable(final String key) {
        return new Runnable() {
            @Override
            public void run() {
                Paper.book().read(key);
            }
        };
    }

    public long runManyTimes(int times, Runnable runnable) {
        long total = 0;
        for (int i = 0; i < times; i++) {
            long start = SystemClock.uptimeMillis();
            runnable.run();
            long end = SystemClock.uptimeMillis();
            total += end - start;
        }
        return total / times;
    }
}
