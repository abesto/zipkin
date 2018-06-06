package zipkin2.storage.mysql.v1;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import org.jooq.DSLContext;
import org.jooq.Record;
import zipkin2.DependencyLink;
import zipkin2.internal.DependencyLinker;

import static zipkin2.storage.mysql.v1.Schema.maybeGet;
import static zipkin2.storage.mysql.v1.internal.generated.tables.ZipkinDependencies.ZIPKIN_DEPENDENCIES;

final class SelectDependencies implements Function<DSLContext, List<DependencyLink>> {
  final Schema schema;
  final List<Date> days;

  SelectDependencies(Schema schema, List<Date> days) {
    this.schema = schema;
    this.days = days;
  }

  @Override
  public List<DependencyLink> apply(DSLContext context) {
    List<DependencyLink> unmerged =
        context
            .select(schema.dependencyLinkFields)
            .from(ZIPKIN_DEPENDENCIES)
            .where(ZIPKIN_DEPENDENCIES.DAY.in(days))
            .fetch(
                (Record l) ->
                    DependencyLink.newBuilder()
                        .parent(l.get(ZIPKIN_DEPENDENCIES.PARENT))
                        .child(l.get(ZIPKIN_DEPENDENCIES.CHILD))
                        .callCount(l.get(ZIPKIN_DEPENDENCIES.CALL_COUNT))
                        .errorCount(maybeGet(l, ZIPKIN_DEPENDENCIES.ERROR_COUNT, 0L))
                        .build());
    return DependencyLinker.merge(unmerged);
  }

  @Override
  public String toString() {
    return "SelectDependencies{days=" + days + "}";
  }
}
