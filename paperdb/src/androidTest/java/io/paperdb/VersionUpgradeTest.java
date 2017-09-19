package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class VersionUpgradeTest {
    @Before
    public void setUp() throws Exception {
        Paper.init(getTargetContext());
        Paper.book().destroy();
    }

    @Test
    public void testWrite() {
        Recipe recipe = new Recipe();
        recipe.name = "chocolate cake";
        recipe.ingredients = new HashMap<String, Integer>();
        recipe.ingredients.put("flour", 300);
        recipe.ingredients.put("eggs", 4);
        recipe.ingredients.put("chocolate", 200);
        recipe.duration = 30;

        Paper.book().write("recipe", recipe);
    }

    @Test
    public void testRead() throws IOException {
        File filesDir = new File(getTargetContext().getFilesDir(), "io.paperdb");
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }

        InputStream inputStream = this.getClass().getResourceAsStream("/recipe_1.5.pt");
        OutputStream outputStream = new FileOutputStream(new File(filesDir, "recipe.pt"));
        copyFile(inputStream, outputStream);
        outputStream.close();

        Recipe recipe = Paper.book().read("recipe");
        assertThat(recipe.name).isEqualTo("chocolate cake");

    }

    private void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte buffer[] = new byte[2048];

        while(true) {
            int r = inputStream.read(buffer);
            if (r < 0) {
                break;
            }

            outputStream.write(buffer);
        }
        outputStream.flush();
    }

    public static class Recipe {
        HashMap<String, Integer> ingredients;
        String name;
        int duration;
    }
}
