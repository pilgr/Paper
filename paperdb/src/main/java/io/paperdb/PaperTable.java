package io.paperdb;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import java.io.Serializable;

@DefaultSerializer(CompatibleFieldSerializer.class)
class PaperTable<T> implements Serializable {

    @SuppressWarnings("UnusedDeclaration") PaperTable() {
    }

    PaperTable(T[] content) {
        this.content = content;
    }

    T[] content;
}
