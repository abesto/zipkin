/**
 * Copyright 2015-2017 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This file is generated by jOOQ.
*/
package zipkin.storage.mysql.internal.generated.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;

import zipkin.storage.mysql.internal.generated.Keys;
import zipkin.storage.mysql.internal.generated.Zipkin;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ZipkinSpans extends TableImpl<Record> {

    private static final long serialVersionUID = 2053840611;

    /**
     * The reference instance of <code>zipkin.zipkin_spans</code>
     */
    public static final ZipkinSpans ZIPKIN_SPANS = new ZipkinSpans();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>zipkin.zipkin_spans.trace_id_high</code>. If non zero, this means the trace uses 128 bit traceIds instead of 64 bit
     */
    public final TableField<Record, Long> TRACE_ID_HIGH = createField("trace_id_high", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.BIGINT)), this, "If non zero, this means the trace uses 128 bit traceIds instead of 64 bit");

    /**
     * The column <code>zipkin.zipkin_spans.trace_id</code>.
     */
    public final TableField<Record, Long> TRACE_ID = createField("trace_id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>zipkin.zipkin_spans.id</code>.
     */
    public final TableField<Record, Long> ID = createField("id", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>zipkin.zipkin_spans.name</code>.
     */
    public final TableField<Record, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

    /**
     * The column <code>zipkin.zipkin_spans.parent_id</code>.
     */
    public final TableField<Record, Long> PARENT_ID = createField("parent_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>zipkin.zipkin_spans.debug</code>.
     */
    public final TableField<Record, Boolean> DEBUG = createField("debug", org.jooq.impl.SQLDataType.BIT, this, "");

    /**
     * The column <code>zipkin.zipkin_spans.start_ts</code>. Span.timestamp(): epoch micros used for endTs query and to implement TTL
     */
    public final TableField<Record, Long> START_TS = createField("start_ts", org.jooq.impl.SQLDataType.BIGINT, this, "Span.timestamp(): epoch micros used for endTs query and to implement TTL");

    /**
     * The column <code>zipkin.zipkin_spans.duration</code>. Span.duration(): micros used for minDuration and maxDuration query
     */
    public final TableField<Record, Long> DURATION = createField("duration", org.jooq.impl.SQLDataType.BIGINT, this, "Span.duration(): micros used for minDuration and maxDuration query");

    /**
     * Create a <code>zipkin.zipkin_spans</code> table reference
     */
    public ZipkinSpans() {
        this("zipkin_spans", null);
    }

    /**
     * Create an aliased <code>zipkin.zipkin_spans</code> table reference
     */
    public ZipkinSpans(String alias) {
        this(alias, ZIPKIN_SPANS);
    }

    private ZipkinSpans(String alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private ZipkinSpans(String alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Zipkin.ZIPKIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<Record>> getKeys() {
        return Arrays.<UniqueKey<Record>>asList(Keys.KEY_ZIPKIN_SPANS_TRACE_ID_HIGH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZipkinSpans as(String alias) {
        return new ZipkinSpans(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ZipkinSpans rename(String name) {
        return new ZipkinSpans(name, null);
    }
}
