package io.paperdb.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Serializes collections without no-arg constructors by creating ArrayList instances to wrap
 * their data.
 * <p/>
 * For example collections created by calling List.sublist() don't have no-arg constructors.
 */
public class NoArgCollectionSerializer extends CollectionSerializer {
    @Override
    protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
        return new ArrayList();
    }
}
