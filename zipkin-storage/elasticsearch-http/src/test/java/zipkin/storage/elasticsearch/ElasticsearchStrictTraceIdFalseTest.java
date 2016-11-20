/**
 * Copyright 2015-2016 The OpenZipkin Authors
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
package zipkin.storage.elasticsearch;

import org.junit.AssumptionViolatedException;
import zipkin.Component;
import zipkin.storage.StorageComponent;

import java.io.IOException;

public abstract class ElasticsearchStrictTraceIdFalseTest extends zipkin.storage.StrictTraceIdFalseTest {

  private final ElasticsearchStorage storage;

  public ElasticsearchStrictTraceIdFalseTest() {
    storage = storageBuilder()
        .strictTraceId(false)
        .index("test_zipkin_http_mixed")
        .build();

    Component.CheckResult check = storage.check();
    if (!check.ok) {
      throw new AssumptionViolatedException(check.exception.getMessage(), check.exception);
    }
  }

  protected abstract ElasticsearchStorage.Builder storageBuilder();

  @Override protected StorageComponent storage() {
    return storage;
  }

  @Override public void clear() throws IOException {
    storage.clear();
  }
}
