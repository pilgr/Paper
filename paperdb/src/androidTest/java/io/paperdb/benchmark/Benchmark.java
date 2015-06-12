package io.paperdb.benchmark;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.orhanobut.hawk.Hawk;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.paperdb.Paper;
import io.paperdb.testdata.Person;
import io.paperdb.testdata.TestDataGenerator;
import util.DelayMeasurer;

import static android.support.test.InstrumentationRegistry.getTargetContext;

// z3c
//        06-05 21:47:05.199    1673-1692/io.paperdb.test D/DelayMeasurer﹕ Paper read/writes/10 102ms
//        06-05 21:47:06.856    1673-1692/io.paperdb.test D/DelayMeasurer﹕ Hawk read/writes/10 165ms
//Genymotion lollipop 100
//        06-08 14:24:18.627    3080-3100/io.paperdb.test D/DelayMeasurer﹕ Paper read/writes/100 18ms
//        06-08 14:24:21.880    3080-3100/io.paperdb.test D/DelayMeasurer﹕ Hawk read/writes/100 32ms

@RunWith(AndroidJUnit4.class)
@LargeTest
public class Benchmark extends AndroidTestCase{

    public static final String BENCHMARK_DATA = "benchmark-data";
    private static int REPEAT_COUNT = 100;

    @Test
    public void testReadWrite100Items() throws Exception {
        final List<Person> contacts = TestDataGenerator.genPersonList(500);

        DelayMeasurer.start("Paper read/writes");
        paperDbReadWrite(contacts, REPEAT_COUNT);
        DelayMeasurer.finish("Paper read/writes", REPEAT_COUNT);

        DelayMeasurer.start("Hawk read/writes");
        hawkReadWrite(contacts, REPEAT_COUNT);
        DelayMeasurer.finish("Hawk read/writes", REPEAT_COUNT);
    }

    private void hawkReadWrite(List data, int repeat) {
        Hawk.init(getTargetContext());
        Hawk.clear();
        for (int i = 0; i < repeat; i++) {
            Hawk.put(BENCHMARK_DATA, data);
            Hawk.get(BENCHMARK_DATA);
        }
    }

    private void paperDbReadWrite(List data, int repeat) {
        Paper.destroy(getTargetContext());
        Paper.init(getTargetContext());
        for (int i = 0; i < repeat; i++) {
            Paper.put(BENCHMARK_DATA, data);
            Paper.get(BENCHMARK_DATA);
        }
    }
}
