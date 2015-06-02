package io.paperdb;

import android.content.Context;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.paperdb.PaperDb.TAG;

public class DbStoragePlainFile extends DbStorageBase {

    private final String mFilesDir;

    public DbStoragePlainFile(Context context, String dbName) {
        mFilesDir = getDbPath(context, dbName);
        if (!new File(mFilesDir).exists()) {
            boolean isReady = new File(mFilesDir).mkdirs();
            if (!isReady) {
                throw new RuntimeException("Couldn't create PaperDb dir: " + mFilesDir);
            }
        }
    }

    @Override
    public void destroy(Context context, String dbName) {
        final String dbPath = getDbPath(context, dbName);
        if (!deleteDirectory(dbPath)) {
            Log.e(TAG, "Couldn't delete PaperDb dir " + dbPath);
        }
    }

    @Override
    public synchronized <E extends Serializable> void insert(String tableName, Collection<E> items) {
        if (items == null || items.size() == 0) {
            deleteIfExists(tableName);
            return;
        }
        //noinspection unchecked
        E[] copy = (E[]) Array.newInstance(Serializable.class, items.size());
        copy = items.toArray(copy);
        final PaperTable<E> paperTable = new PaperTable<>(copy);

        final File originalFile = makeOriginalFile(tableName);
        final File backupFile = makeBackupFile(originalFile);
        // Rename the current file so it may be used as a backup during the next read
        if (originalFile.exists()) {
            //Rename original to backup
            if (!backupFile.exists()) {
                if (!originalFile.renameTo(backupFile)) {
                    throw new PaperDbException("Couldn't rename file " + originalFile
                            + " to backup file " + backupFile);
                }
            } else {
                //Backup exist -> original file is broken and must be deleted
                //noinspection ResultOfMethodCallIgnored
                originalFile.delete();
            }
        }

        // Attempt to write the file, delete the backup and return true as atomically as
        // possible.  If any exception occurs, delete the new file; next time we will restore
        // from the backup.
        try {
            FileOutputStream fileStream = new FileOutputStream(originalFile);

            final Output kryoOutput = new Output(fileStream);
            getKryo().writeObject(kryoOutput, paperTable);
            kryoOutput.flush();
            fileStream.flush();
            sync(fileStream);
            kryoOutput.close(); //also close file stream

            // Writing was successful, delete the backup file if there is one.
            //noinspection ResultOfMethodCallIgnored
            backupFile.delete();
        } catch (IOException | KryoException e) {
            // Clean up an unsuccessfully written file
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new PaperDbException("Couldn't clean up partially-written file " + originalFile, e);
                }
            }
            throw new PaperDbException("Couldn't save table: " + tableName + ". " +
                    "Backed up table will be used on next read attempt", e);
        }
    }

    private File makeOriginalFile(String tableName) {
        //TODO check valid file name/path with regexp
        final String tablePath = mFilesDir + File.separator + tableName + ".pt";
        return new File(tablePath);
    }

    @Override
    public synchronized <E extends Serializable> List<E> select(String tableName) {
        List<E> items = new ArrayList<>();

        final File originalFile = makeOriginalFile(tableName);
        final File backupFile = makeBackupFile(originalFile);
        if (backupFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            originalFile.delete();
            //noinspection ResultOfMethodCallIgnored
            backupFile.renameTo(originalFile);
        }

        if (!exist(tableName)) {
            return items;
        }

        final Kryo kryo = getKryo();
        try {
            final Input i = new Input(new FileInputStream(originalFile));
            //noinspection unchecked
            final PaperTable<E> paperTable = kryo.readObject(i, PaperTable.class);
            i.close();
            if (paperTable.content != null) {
                items = new ArrayList<>(Arrays.asList(paperTable.content));
            }
        } catch (FileNotFoundException | KryoException e) {
            // Clean up an unsuccessfully written file
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new PaperDbException("Couldn't clean up broken/unserializable file " + originalFile, e);
                }
            }
            throw new PaperDbException("Couldn't read/deserialize file " + originalFile + " for table " + tableName, e);
        }
        return items;
    }

    @Override
    public synchronized boolean exist(String tableName) {
        final File originalFile = makeOriginalFile(tableName);
        return originalFile.exists();
    }

    @Override
    public synchronized void deleteIfExists(String tableName) {
        final File originalFile = makeOriginalFile(tableName);
        if (!originalFile.exists()) {
            return;
        }

        boolean deleted = originalFile.delete();
        if (!deleted) {
            throw new PaperDbException("Couldn't delete file " + originalFile + " for table " + tableName);
        }
    }

    private String getDbPath(Context context, String dbName) {
        return context.getFilesDir() + File.separator + dbName;
    }

    //TODO Improve
    private static boolean deleteDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.toString());
                    } else {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    private File makeBackupFile(File originalFile) {
        return new File(originalFile.getPath() + ".bak");
    }

    /**
     * Perform an fsync on the given FileOutputStream.  The stream at this
     * point must be flushed but not yet closed.
     */
    private static boolean sync(FileOutputStream stream) {
        //noinspection EmptyCatchBlock
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
            return true;
        } catch (IOException e) {
        }
        return false;
    }
}

