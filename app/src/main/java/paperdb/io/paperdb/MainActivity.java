package paperdb.io.paperdb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.List;

import io.paperdb.Paper;

import static java.util.Arrays.asList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String PERSON = "person";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        findViewById(R.id.test_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LongHolder o1 = new LongHolder(12L);
                LongListHolder o2 = new LongListHolder(asList(23L));
                Paper.book().write("o1", o1);
                Paper.book().write("o2", o2);
            }
        });

        final Button btnRead = (Button) findViewById(R.id.test_read);

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LongHolder o1 = Paper.book().read("o1", new LongHolder(-1L));
                LongListHolder o2 = Paper.book().read("o2", new LongListHolder(asList(-1L)));

                //long lastModified = Paper.book().lastModified("o1");
                //Log.d(TAG, "lastModified: " + lastModified);

                btnRead.setText("Read: " + o1.getValue() + " : " + o2.getValue().get(0));
            }
        });
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
}
