package paperdb.io.paperdb;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        findViewById(R.id.test_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleWrite().execute();
            }
        });

        final Button btnRead = findViewById(R.id.test_read);

        btnRead.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                new SimpleRead(btnRead).execute();
            }
        });

        final Button btnReadWriteAsync = findViewById(R.id.test_read_write_async);
        btnReadWriteAsync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testReadWriteAsync();
            }
        });
    }

    private void testReadWriteAsync() {
        final int iterations = 1000;
        writeReadDestroy("key1", iterations);
        writeReadDestroy("key2", iterations);
    }

    @SuppressWarnings("SameParameterValue")
    private void writeReadDestroy(final String key, final int iterations) {
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < iterations; i++) {
                    Paper.book().write(key, "key:" + key + " iteration#" + i);
                    Log.d(Thread.currentThread().getName(), "All keys:" + Paper.book().getAllKeys().toString());
                    Log.d(Thread.currentThread().getName(), "read key:" + key + "=" + Paper.book().<String>read(key));
                    // This caused the issue on multi-thread paper db dir creation
                    Paper.book().destroy();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class Person {
        final String name;

        Person(String name) {
            this.name = name;
        }
    }

    interface Holder<V> {
        V getValue();
    }

    static abstract class AbstractValueHolder<V> implements Holder<V> {
        private final V value;

        AbstractValueHolder(V value) {
            this.value = value;
        }

        @Override
        public V getValue() {
            return value;
        }
    }

    static abstract class AbstractValueListHolder<V> extends AbstractValueHolder<List<V>> {
        AbstractValueListHolder(List<V> value) {
            super(value);
        }
    }

    static class LongHolder extends AbstractValueHolder<Long> {
        LongHolder(Long value) {
            super(value);
        }
    }

    static class LongListHolder extends AbstractValueListHolder<Long> {
        LongListHolder(List<Long> value) {
            super(value);
        }
    }

    // So fun to write async tasks in java in 2020. How did we live with that?
    private static class SimpleRead extends AsyncTask<Void, Void, ArrayList<AbstractValueHolder>> {
        private final WeakReference<Button> wrBtnRead;

        private SimpleRead(Button btnRead) {
            wrBtnRead = new WeakReference<>(btnRead);
        }

        @Override
        protected ArrayList<AbstractValueHolder> doInBackground(Void... voids) {
            LongHolder o1 = Paper.book().read("o1", new LongHolder(-1L));
            LongListHolder o2 = Paper.book().read("o2",
                    new LongListHolder(Collections.singletonList(-1L)));
            ArrayList<AbstractValueHolder> result = new ArrayList<>();
            result.add(o1);
            result.add(o2);
            return result;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(ArrayList<AbstractValueHolder> result) {
            Button btnRead = wrBtnRead.get();
            LongHolder o1 = (LongHolder) result.get(0);
            LongListHolder o2 = (LongListHolder) result.get(1);
            if (btnRead != null) {
                btnRead.setText("Read: " + o1.getValue() + " : " + o2.getValue().get(0));
            }
        }
    }

    private static class SimpleWrite extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            LongHolder o1 = new LongHolder(12L);
            LongListHolder o2 = new LongListHolder(Collections.singletonList(23L));
            Paper.book().write("o1", o1);
            Paper.book().write("o2", o2);
            return null;
        }
    }

}
