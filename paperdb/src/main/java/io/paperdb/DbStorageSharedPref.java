package io.paperdb;

import android.content.Context;
import android.content.SharedPreferences;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class DbStorageSharedPref extends DbStorageBase {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final SharedPreferences prefDb;


    public DbStorageSharedPref(Context context, String dbName) {
        prefDb = createSharedPrefDbInstance(context, dbName);
    }

    private SharedPreferences createSharedPrefDbInstance(Context context, String dbName) {
        return context.getSharedPreferences(dbName, Context.MODE_PRIVATE);
    }

    @Override
    public void destroy(Context context, String dbName) {
        SharedPreferences db = createSharedPrefDbInstance(context, dbName);
        db.edit().clear().apply();
    }

    @Override
    public <E extends Serializable> void insert(String tableName, Collection<E> items) {
        if (items == null || items.size() == 0) {
            deleteIfExists(tableName);
            return;
        }
        final Kryo kryo = getKryo();
        //noinspection unchecked
        E[] copy = (E[]) Array.newInstance(Serializable.class, items.size());
        copy = items.toArray(copy);
        final PaperTable<E> paperTable = new PaperTable<>(copy);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(items.size() * 10);
        final Output o = new Output(bos);
        kryo.writeObject(o, paperTable);
        o.flush();

        byte[] tableBytes = bos.toByteArray();
        String tableHex = bytesToHex(tableBytes);

        o.close();

        prefDb.edit().putString(tableName, tableHex).apply();
    }

    @Override
    public <E extends Serializable> List<E> select(String tableName) {
        List<E> items = new ArrayList<>();
        final Kryo kryo = getKryo();
        String tableHex = prefDb.getString(tableName, null);
        if (tableHex == null) {
            return items;
        }

        byte[] tableBytes = hexStringToByteArray(tableHex);
        final Input i = new Input(new ByteArrayInputStream(tableBytes));
        //noinspection unchecked
        final PaperTable<E> paperTable = kryo.readObject(i, PaperTable.class);
        i.close();
        if (paperTable.content != null) {
            items = new ArrayList<>(Arrays.asList(paperTable.content));
        }
        return items;
    }

    @Override
    public boolean exist(String tableName) {
        String val = prefDb.getString(tableName, null);
        return val != null;
    }

    @Override
    public void deleteIfExists(String tableName) {
        prefDb.edit().remove(tableName).apply();
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
