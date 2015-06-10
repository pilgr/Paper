package io.paperdb.benchmark;

import android.test.AndroidTestCase;

import com.orhanobut.hawk.Hawk;

import java.util.List;

import io.paperdb.Paper;
import io.paperdb.Person;
import util.DelayMeasurer;
import util.TestDataGenerator;

// z3c
//        06-05 21:47:05.199    1673-1692/io.paperdb.test D/DelayMeasurer﹕ Paper read/writes/10 102ms
//        06-05 21:47:06.856    1673-1692/io.paperdb.test D/DelayMeasurer﹕ Hawk read/writes/10 165ms
//Genymotion lollipop 100
//        06-08 14:24:18.627    3080-3100/io.paperdb.test D/DelayMeasurer﹕ Paper read/writes/100 18ms
//        06-08 14:24:21.880    3080-3100/io.paperdb.test D/DelayMeasurer﹕ Hawk read/writes/100 32ms


public class Benchmark extends AndroidTestCase{

    public static final String BENCHMARK_DATA = "benchmark-data";
    private static int REPEAT_COUNT = 100;

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
        Hawk.init(getContext());
        Hawk.clear();
        for (int i = 0; i < repeat; i++) {
            Hawk.put(BENCHMARK_DATA, data);
            Hawk.get(BENCHMARK_DATA);
        }
    }

    private void paperDbReadWrite(List data, int repeat) {
        Paper.destroy(getContext());
        Paper.init(getContext());
        for (int i = 0; i < repeat; i++) {
            Paper.putList(BENCHMARK_DATA, data);
            Paper.getList(BENCHMARK_DATA);
        }
    }
}
