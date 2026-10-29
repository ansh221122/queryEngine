package query.tree;

import java.util.List;
import java.util.Map;

public interface Executor {
    <T> List<T> execute(Query<T> query, List<T> data);
    <T> List<JoinedRow> executeWithJoin(Query<T> query, Map<Class<?>, List<?>> entityListDataMap);
}