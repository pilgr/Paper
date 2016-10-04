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

package io.paperdb;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;
import org.joda.time.chrono.JulianChronology;

/**
 * A format for joda {@link DateTime}, that stores the millis, chronology and
 * time zone as separate attributes. If the chronology is {@link ISOChronology},
 * the attribute is omitted, thus {@link ISOChronology} is seen as default. If
 * the time zone is the default time zone ({@link DateTimeZone#getDefault()}),
 * the time zone attribute is omitted. This requires different machines to
 * have the same time zone settings.
 * <p>
 * The following chronologies are supported:
 * <ul>
 * <li>{@link ISOChronology}</li>
 * <li>{@link CopticChronology}</li>
 * <li>{@link EthiopicChronology}</li>
 * <li>{@link GregorianChronology}</li>
 * <li>{@link JulianChronology}</li>
 * <li>{@link IslamicChronology}</li>
 * <li>{@link BuddhistChronology}</li>
 * <li>{@link GJChronology}</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 * @version 9a4f71e
 */
public class JodaDateTimeSerializer extends Serializer<DateTime> {

    static final String MILLIS = "millis";
    static final String DATE_TIME = "dt";
    static final String CHRONOLOGY = "ch";
    static final String TIME_ZONE = "tz";

    public JodaDateTimeSerializer() {
        setImmutable(true);
    }

    @Override
    public DateTime read(final Kryo kryo, final Input input, final Class<DateTime> type) {
        final long millis = input.readLong(true);
        final Chronology chronology = IdentifiableChronology.readChronology( input );
        final DateTimeZone tz = readTimeZone( input );
        return new DateTime( millis, chronology.withZone( tz ) );
    }

    @Override
    public void write(final Kryo kryo, final Output output, final DateTime obj) {
        output.writeLong(obj.getMillis(), true);

        final String chronologyId = IdentifiableChronology.getChronologyId( obj.getChronology() );
        output.writeString(chronologyId == null ? "" : chronologyId);

        output.writeString(obj.getZone().getID());
    }

    private DateTimeZone readTimeZone( final Input input ) {
        final String tz = input.readString();

        // special case for "" to maintain backwards compatibility, but generally this is considered harmful,
        // potentially remove this with the next major release that involves breaking changes
        // https://github.com/magro/kryo-serializers/issues/30
        if ("".equals(tz)) {
            return DateTimeZone.getDefault();
        }

        return DateTimeZone.forID(tz);
    }
}