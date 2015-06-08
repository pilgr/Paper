package io.paperdb;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.VersionFieldSerializer;

import java.util.Collection;
import java.util.List;

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

    public abstract <E> void insert(String tableName, Collection<E> items);

    public abstract <E> List<E> select(String tableName);

    public abstract boolean exist(String tableName);

    public abstract void deleteIfExists(String tableName);
}
