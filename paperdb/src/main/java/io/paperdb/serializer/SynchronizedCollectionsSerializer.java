/*
 * Copyright 2010 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.paperdb.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A kryo {@link Serializer} for synchronized {@link Collection}s and {@link Map}s
 * created via {@link Collections}.
 *
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 * @version 9a4f71e
 */
public class SynchronizedCollectionsSerializer extends Serializer<Object> {

    private static final Field SOURCE_COLLECTION_FIELD;
    private static final Field SOURCE_MAP_FIELD;

    static {
        try {
            SOURCE_COLLECTION_FIELD = Class.forName("java.util.Collections$SynchronizedCollection" )
                    .getDeclaredField( "c" );
            SOURCE_COLLECTION_FIELD.setAccessible( true );

            SOURCE_MAP_FIELD = Class.forName("java.util.Collections$SynchronizedMap" )
                    .getDeclaredField( "m" );
            SOURCE_MAP_FIELD.setAccessible( true );
        } catch ( final Exception e ) {
            throw new RuntimeException( "Could not access source collection" +
                    " field in java.util.Collections$SynchronizedCollection.", e );
        }
    }

    @Override
    public Object read(final Kryo kryo, final Input input, final Class<Object> clazz) {
        final int ordinal = input.readInt( true );
        final SynchronizedCollection collection = SynchronizedCollection.values()[ordinal];
        final Object sourceCollection = kryo.readClassAndObject( input );
        return collection.create( sourceCollection );
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Object object) {
        try {
            final SynchronizedCollection collection = SynchronizedCollection.valueOfType( object.getClass() );
            // the ordinal could be replaced by s.th. else (e.g. a explicitely managed "id")
            output.writeInt( collection.ordinal(), true );
            kryo.writeClassAndObject( output, collection.sourceCollectionField.get( object ) );
        } catch ( final RuntimeException e ) {
            // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
            // handles SerializationException specifically (resizing the buffer)...
            throw e;
        } catch ( final Exception e ) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public Object copy(Kryo kryo, Object original) {
        try {
            final SynchronizedCollection collection = SynchronizedCollection.valueOfType( original.getClass() );
            Object sourceCollectionCopy = kryo.copy(collection.sourceCollectionField.get(original));
            return collection.create( sourceCollectionCopy );
        } catch ( final RuntimeException e ) {
            // Don't eat and wrap RuntimeExceptions
            throw e;
        } catch ( final Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private static enum SynchronizedCollection {
        COLLECTION( Collections.synchronizedCollection( Arrays.asList( "" ) ).getClass(), SOURCE_COLLECTION_FIELD ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedCollection( (Collection<?>) sourceCollection );
            }
        },
        RANDOM_ACCESS_LIST( Collections.synchronizedList( new ArrayList<Void>() ).getClass(), SOURCE_COLLECTION_FIELD ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedList( (List<?>) sourceCollection );
            }
        },
        LIST( Collections.synchronizedList( new LinkedList<Void>() ).getClass(), SOURCE_COLLECTION_FIELD ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedList( (List<?>) sourceCollection );
            }
        },
        SET( Collections.synchronizedSet( new HashSet<Void>() ).getClass(), SOURCE_COLLECTION_FIELD ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedSet( (Set<?>) sourceCollection );
            }
        },
        SORTED_SET( Collections.synchronizedSortedSet( new TreeSet<Void>() ).getClass(), SOURCE_COLLECTION_FIELD ){
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedSortedSet( (SortedSet<?>) sourceCollection );
            }
        },
        MAP( Collections.synchronizedMap( new HashMap<Void, Void>() ).getClass(), SOURCE_MAP_FIELD ) {

            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedMap( (Map<?, ?>) sourceCollection );
            }

        },
        SORTED_MAP( Collections.synchronizedSortedMap( new TreeMap<Void, Void>() ).getClass(), SOURCE_MAP_FIELD ) {
            @Override
            public Object create( final Object sourceCollection ) {
                return Collections.synchronizedSortedMap( (SortedMap<?, ?>) sourceCollection );
            }
        };

        private final Class<?> type;
        private final Field sourceCollectionField;

        private SynchronizedCollection( final Class<?> type, final Field sourceCollectionField ) {
            this.type = type;
            this.sourceCollectionField = sourceCollectionField;
        }

        /**
         * @param sourceCollection
         */
        public abstract Object create( Object sourceCollection );

        static SynchronizedCollection valueOfType( final Class<?> type ) {
            for( final SynchronizedCollection item : values() ) {
                if ( item.type.equals( type ) ) {
                    return item;
                }
            }
            throw new IllegalArgumentException( "The type " + type + " is not supported." );
        }

    }

    /**
     * Creates a new {@link SynchronizedCollectionsSerializer} and registers its serializer
     * for the several synchronized Collections that can be created via {@link Collections},
     * including {@link Map}s.
     *
     * @param kryo the {@link Kryo} instance to set the serializer on.
     *
     * @see Collections#synchronizedCollection(Collection)
     * @see Collections#synchronizedList(List)
     * @see Collections#synchronizedSet(Set)
     * @see Collections#synchronizedSortedSet(SortedSet)
     * @see Collections#synchronizedMap(Map)
     * @see Collections#synchronizedSortedMap(SortedMap)
     */
    public static void registerSerializers( final Kryo kryo ) {
        final SynchronizedCollectionsSerializer serializer = new SynchronizedCollectionsSerializer();
        SynchronizedCollection.values();
        for ( final SynchronizedCollection item : SynchronizedCollection.values() ) {
            kryo.register( item.type, serializer );
        }
    }

}