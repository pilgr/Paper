package io.paperdb;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;

import io.paperdb.utils.TestUtils;

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
        recipe.ingredients = new HashMap<>();
        recipe.ingredients.put("flour", 300);
        recipe.ingredients.put("eggs", 4);
        recipe.ingredients.put("chocolate", 200);
        recipe.duration = 30;

        Paper.book().write("recipe", recipe);
    }

    @Test
    public void testRead() throws IOException {
        TestUtils.replacePaperDbFileBy("recipe_1.5.pt", "recipe");

        Recipe recipe = Paper.book().read("recipe");
        assertThat(recipe.name).isEqualTo("chocolate cake");
    }

    public static class Recipe {
        HashMap<String, Integer> ingredients;
        String name;
        int duration;
    }
}
