package io.paperdb;

import android.content.Context;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import io.paperdb.serializer.NoArgCollectionSerializer;

import static io.paperdb.Paper.TAG;

public class DbStoragePlainFile implements Storage {

    private final Context mContext;
    private final String mDbName;
    private final HashMap<Class, Serializer> mCustomSerializers;
    private String mFilesDir;
    private boolean mPaperDirIsCreated;
    private KeyLocker keyLocker = new KeyLocker();

    private Kryo getKryo() {
        return mKryo.get();
    }

    private final ThreadLocal<Kryo> mKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return createKryoInstance(false);
        }
    };

    private Kryo createKryoInstance(boolean compatibilityMode) {
        Kryo kryo = new Kryo();

        if (compatibilityMode) {
            kryo.getFieldSerializerConfig().setOptimizedGenerics(true);
        }

        kryo.register(PaperTable.class);
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        kryo.setReferences(false);

        // Serialize Arrays$ArrayList
        //noinspection ArraysAsListWithZeroOrOneArgument
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
        UnmodifiableCollectionsSerializer.registerSerializers(kryo);
        SynchronizedCollectionsSerializer.registerSerializers(kryo);
        // Serialize inner AbstractList$SubAbstractListRandomAccess
        kryo.addDefaultSerializer(new ArrayList<>().subList(0, 0).getClass(),
                new NoArgCollectionSerializer());
        // Serialize AbstractList$SubAbstractList
        kryo.addDefaultSerializer(new LinkedList<>().subList(0, 0).getClass(),
                new NoArgCollectionSerializer());
        // To keep backward compatibility don't change the order of serializers above

        // UUID support
        kryo.register(UUID.class, new UUIDSerializer());

        for (Class<?> clazz : mCustomSerializers.keySet())
            kryo.register(clazz, mCustomSerializers.get(clazz));

        kryo.setInstantiatorStrategy(
                new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        return kryo;
    }

    DbStoragePlainFile(Context context, String dbName,
                       HashMap<Class, Serializer> serializers) {
        mContext = context;
        mDbName = dbName;
        mCustomSerializers = serializers;
    }

    @Override
    public synchronized void destroy() {
        assertInit();

        final String dbPath = getDbPath(mContext, mDbName);
        if (!deleteDirectory(dbPath)) {
            Log.e(TAG, "Couldn't delete Paper dir " + dbPath);
        }
        mPaperDirIsCreated = false;
    }

    @Override
    public <E> void insert(String key, E value) {
        try {
            keyLocker.acquire(key);
            assertInit();

            final PaperTable<E> paperTable = new PaperTable<>(value);

            final File originalFile = getOriginalFile(key);
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

            writeTableFile(key, paperTable, originalFile, backupFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                keyLocker.release(key);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public <E> E select(String key) {
        try {
            keyLocker.acquire(key);
            assertInit();

            final File originalFile = getOriginalFile(key);
            final File backupFile = makeBackupFile(originalFile);
            if (backupFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                originalFile.delete();
                //noinspection ResultOfMethodCallIgnored
                backupFile.renameTo(originalFile);
            }

            if (!exist(key)) {
                return null;
            }

            return readTableFile(key, originalFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                keyLocker.release(key);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean exist(String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        return originalFile.exists();
    }

    @Override
    public synchronized long lastModified(String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        return originalFile.exists() ? originalFile.lastModified() : -1;
    }

    @Override
    public List<String> getAllKeys() {
        assertInit();

        File bookFolder = new File(mFilesDir);
        String[] names = bookFolder.list();
        if (names != null) {
            //remove extensions
            for (int i = 0; i < names.length; i++) {
                names[i] = names[i].replace(".pt", "");
            }
            return Arrays.asList(names);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteIfExists(String key) {
        try {
            keyLocker.acquire(key);
            assertInit();

            final File originalFile = getOriginalFile(key);
            if (!originalFile.exists()) {
                return;
            }

            boolean deleted = originalFile.delete();
            if (!deleted) {
                throw new PaperDbException("Couldn't delete file " + originalFile
                        + " for table " + key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                keyLocker.release(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setLogLevel(int level) {
        com.esotericsoftware.minlog.Log.set(level);
    }

    private File getOriginalFile(String key) {
        final String tablePath = mFilesDir + File.separator + key + ".pt";
        return new File(tablePath);
    }

    /**
     * Attempt to write the file, delete the backup and return true as atomically as
     * possible.  If any exception occurs, delete the new file; next time we will restore
     * from the backup.
     *
     * @param key          table key
     * @param paperTable   table instance
     * @param originalFile file to write new data
     * @param backupFile   backup file to be used if write is failed
     */
    private <E> void writeTableFile(String key, PaperTable<E> paperTable,
                                    File originalFile, File backupFile) {
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
                    throw new PaperDbException("Couldn't clean up partially-written file "
                            + originalFile, e);
                }
            }
            throw new PaperDbException("Couldn't save table: " + key + ". " +
                    "Backed up table will be used on next read attempt", e);
        }
    }

    private <E> E readTableFile(String key, File originalFile) {
        try {
            return readContent(originalFile, getKryo());
        } catch (FileNotFoundException | KryoException | ClassCastException e) {
            Throwable exception = e;
            // Give one more chance, read data in paper 1.x compatibility mode
            if (e instanceof KryoException) {
                try {
                    return readContent(originalFile, createKryoInstance(true));
                } catch (FileNotFoundException | KryoException | ClassCastException compatibleReadException) {
                    exception = compatibleReadException;
                }
            }
            String errorMessage = "Couldn't read/deserialize file "
                    + originalFile + " for table " + key;
            throw new PaperDbException(errorMessage, exception);
        }
    }

    private <E> E readContent(File originalFile, Kryo kryo) throws FileNotFoundException, KryoException {
        final Input i = new Input(new FileInputStream(originalFile));
        //noinspection TryFinallyCanBeTryWithResources
        try {
            //noinspection unchecked
            final PaperTable<E> paperTable = kryo.readObject(i, PaperTable.class);
            return paperTable.mContent;
        } finally {
            i.close();
        }
    }

    private String getDbPath(Context context, String dbName) {
        return context.getFilesDir() + File.separator + dbName;
    }

    private void assertInit() {
        if (!mPaperDirIsCreated) {
            createPaperDir();
            mPaperDirIsCreated = true;
        }
    }

    private void createPaperDir() {
        mFilesDir = getDbPath(mContext, mDbName);
        if (!new File(mFilesDir).exists()) {
            boolean isReady = new File(mFilesDir).mkdirs();
            if (!isReady) {
                throw new RuntimeException("Couldn't create Paper dir: " + mFilesDir);
            }
        }
    }

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
        return directory.delete();
    }

    private File makeBackupFile(File originalFile) {
        return new File(originalFile.getPath() + ".bak");
    }

    /**
     * Perform an fsync on the given FileOutputStream.  The stream at this
     * point must be flushed but not yet closed.
     */
    private static void sync(FileOutputStream stream) {
        //noinspection EmptyCatchBlock
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
        } catch (IOException e) {
        }
    }
}

