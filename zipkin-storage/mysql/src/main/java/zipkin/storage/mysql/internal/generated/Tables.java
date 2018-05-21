/**
 * Copyright 2015-2018 The OpenZipkin Authors
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
package zipkin.storage.mysql.internal.generated;


import javax.annotation.Generated;

import zipkin.storage.mysql.internal.generated.tables.ZipkinAnnotations;
import zipkin.storage.mysql.internal.generated.tables.ZipkinDependencies;
import zipkin.storage.mysql.internal.generated.tables.ZipkinSpans;


/**
 * Convenience access to all tables in zipkin
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>zipkin.zipkin_annotations</code>.
     */
    public static final ZipkinAnnotations ZIPKIN_ANNOTATIONS = zipkin.storage.mysql.internal.generated.tables.ZipkinAnnotations.ZIPKIN_ANNOTATIONS;

    /**
     * The table <code>zipkin.zipkin_dependencies</code>.
     */
    public static final ZipkinDependencies ZIPKIN_DEPENDENCIES = zipkin.storage.mysql.internal.generated.tables.ZipkinDependencies.ZIPKIN_DEPENDENCIES;

    /**
     * The table <code>zipkin.zipkin_spans</code>.
     */
    public static final ZipkinSpans ZIPKIN_SPANS = zipkin.storage.mysql.internal.generated.tables.ZipkinSpans.ZIPKIN_SPANS;
}
