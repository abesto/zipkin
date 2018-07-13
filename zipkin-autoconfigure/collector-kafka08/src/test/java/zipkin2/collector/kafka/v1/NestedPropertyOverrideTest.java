/*
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
package zipkin2.collector.kafka08;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zipkin2.autoconfigure.collector.kafka08.Access;
import zipkin2.storage.InMemoryStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

public class NestedPropertyOverrideTest {
  @Test
  public void overrideWithNestedProperties() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    addEnvironment(
        context,
        "zipkin.collector.kafka.zookeeper:localhost",
        "zipkin.collector.kafka.overrides.auto.offset.reset:largest");
    Access.registerKafkaProperties(context);
    context.refresh();

    assertThat(
            Access.collectorBuilder(context)
                .storage(InMemoryStorage.newBuilder().build())
                .build()
                .connector
                .config
                .autoOffsetReset())
        .isEqualTo("largest");
  }
}
