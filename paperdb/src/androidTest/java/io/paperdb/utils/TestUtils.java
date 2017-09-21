package io.paperdb.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.support.test.InstrumentationRegistry.getTargetContext;

public class TestUtils {

    public static void replacePaperDbFileBy(String fileName, String asKey) throws IOException {
        File filesDir = new File(getTargetContext().getFilesDir(), "io.paperdb");
        if (!filesDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            filesDir.mkdirs();
        }

        InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(fileName);
        OutputStream outputStream = new FileOutputStream(new File(filesDir, asKey + ".pt"));
        copyFile(inputStream, outputStream);
        outputStream.close();
    }

    private static void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte buffer[] = new byte[2048];

        while (true) {
            int r = inputStream.read(buffer);
            if (r < 0) {
                break;
            }

            outputStream.write(buffer);
        }
        outputStream.flush();
    }
}
