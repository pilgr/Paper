package io.paperdb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

class KryoCypher {
    private static final Boolean LOGS_ENABLED = false;

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "KryoCypherAlias";

    private static final int KRYO_BUFFER_SIZE = 4096;
    private static final int KRYO_MAX_BUFFER_SIZE = -1;

    private static final String SHARED_PREFS_FILENAME = "KryoCypherIVs";

    private final Kryo mKryo;
    private final KeyStore mKeystore;

    private final SharedPreferences mSharedPreferences;

    KryoCypher(String dbPath, Kryo kryo) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException {
        mKryo = kryo;
        mKeystore = KeyStore.getInstance(ANDROID_KEY_STORE);
        mKeystore.load(null);
        mSharedPreferences = DependenciesProvider.getInstance()
                .getApplicationContext()
                .getSharedPreferences(getSharedPrefsFilename(dbPath), Context.MODE_PRIVATE);
    }

    <T> byte[] encrypt(String key, PaperTable<T> object) throws
            NoSuchPaddingException, NoSuchAlgorithmException,
            UnrecoverableEntryException, KeyStoreException, NoSuchProviderException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException {

        Output output = new Output(KRYO_BUFFER_SIZE, KRYO_MAX_BUFFER_SIZE);
        mKryo.writeClassAndObject(output, object);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());

        saveIv(key, cipher.getIV());

        return cipher.doFinal(output.getBuffer());
    }

    @SuppressWarnings("unchecked")
    <T> T decrypt(String key, byte[] encodedData) throws NoSuchAlgorithmException,
            UnrecoverableEntryException, KeyStoreException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(128, getIv(key));
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);

        byte[] decodedData = cipher.doFinal(encodedData);

        return (T) mKryo.readClassAndObject(new Input(decodedData));
    }

    private SecretKey getSecretKey() throws KeyStoreException, UnrecoverableEntryException,
            NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if (mKeystore.containsAlias(KEY_ALIAS)) {
            KeyStore.Entry keystoreEntry = mKeystore.getEntry(KEY_ALIAS, null);
            return ((KeyStore.SecretKeyEntry) keystoreEntry).getSecretKey();
        } else {
            final KeyGenerator keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());

            return keyGenerator.generateKey();
        }
    }

    @SuppressLint("ApplySharedPref")
    private void saveIv(String key, byte[] iv) {
        log("Saving IV, content: %s, key:%s", Arrays.toString(iv), key);
        String ivString = new String(Base64.encode(iv, Base64.DEFAULT), StandardCharsets.US_ASCII);
        mSharedPreferences.edit().putString(key, ivString).commit();
        log("Saving IV, base64:%s, for key:%s ", ivString, key);
    }

    private byte[] getIv(String key) {
        String encodedIvString = mSharedPreferences.getString(key, "");
        log("Restoring IV, base64:%s, for key:", encodedIvString, key);
        byte[] decodedIv = Base64.decode(encodedIvString.getBytes(StandardCharsets.US_ASCII),
                Base64.DEFAULT);
        log("Restoring IV, content:%s, for key:%s", Arrays.toString(decodedIv), key);
        return decodedIv;
    }

    private void log(String log, Object... varargs) {
        if (LOGS_ENABLED) {
            Log.d(getClass().getSimpleName(), String.format(log, varargs));
        }
    }

    private String getSharedPrefsFilename(String databasePath) {
        String filename = SHARED_PREFS_FILENAME + md5(databasePath);
        log("Generated shared places %s", filename);
        return filename;
    }

    private String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
