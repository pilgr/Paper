package io.paperdb;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.VersionFieldSerializer;

public abstract class DbStorageBase {
    protected Kryo getKryo() {
        return kryos.get();
    }

    private ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(PaperTable.class);
            kryo.setDefaultSerializer(VersionFieldSerializer.class);
            return kryo;
        }
    };

    public abstract void destroy();

    public abstract <E> void insert(String key, E value);

    public abstract <E> E select(String key, E defaultValue);

    public abstract boolean exist(String key);

    public abstract void deleteIfExists(String key);
}
