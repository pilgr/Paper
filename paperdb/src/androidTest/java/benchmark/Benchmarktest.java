package benchmark;

import android.test.AndroidTestCase;

import com.orhanobut.hawk.Hawk;

import java.util.List;

import io.paperdb.PaperDb;
import io.paperdb.Person;
import util.DelayMeasurer;
import util.TestDataGenerator;

// z3c
//        06-05 21:47:05.199    1673-1692/io.paperdb.test D/DelayMeasurer﹕ PaperDb read/writes/10 102ms
//        06-05 21:47:06.856    1673-1692/io.paperdb.test D/DelayMeasurer﹕ Hawk read/writes/10 165ms


public class BenchmarkTest extends AndroidTestCase{

    public static final String BENCHMARK_DATA = "benchmark-data";
    private static int REPEAT_COUNT = 10;

    public void testReadWrite100Items() throws Exception {
        final List<Person> contacts = TestDataGenerator.genPersonList(500);

        DelayMeasurer.start("PaperDb read/writes");
        paperDbReadWrite(contacts, REPEAT_COUNT);
        DelayMeasurer.finish("PaperDb read/writes", REPEAT_COUNT);

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
        PaperDb.destroy(getContext(), "benchmark-db");
        PaperDb paperDb = new PaperDb(getContext(), "benchmark-db");
        for (int i = 0; i < repeat; i++) {
            paperDb.insert(BENCHMARK_DATA, data);
            paperDb.select(BENCHMARK_DATA);
        }
    }
}
