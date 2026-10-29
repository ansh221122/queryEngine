package reflection.utility;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import query.tree.JoinedRow;
import query.tree.Query;
import query.tree.QueryPlan;
import util.GroupMetricsManager;
import util.MetricsManager;

public interface EntityManager<T> {
void persist(T t) throws SQLException, IllegalArgumentException, IllegalAccessException;
boolean inspectUnique(T t) throws Exception;
void update(T t) throws SQLException, IllegalArgumentException, IllegalAccessException;

static <T>EntityManager<T> of(Class<T> class1) {
	// TODO Auto-generated method stub
	return new H2EntityManager<T>();
}

public void updateBasedOnUnique(T t) throws SQLException, IllegalArgumentException, IllegalAccessException;

T find(Class<T> class1, Object primaryKey) throws Exception;
List<T> find(Class<T> class1, Predicate<T> predicate) throws Exception;
List<T> pivotColumn(Class<T> clss, String pivotColumnName) throws Exception;
List<T> pivotColumn(Class<T> clss, String[] pivotColumnName) throws Exception;

List<T> findAndProcess(Class<T> class1, Predicate<T> predicate, Function<T, T> function) throws Exception;


List find(Class<T> class1,String columnName, Object columnValue) throws Exception;

//T find(Class<T> class1,ColumnField , Object columnKey) throws Exception;
public void update(T t,String columnName) throws SQLException, IllegalArgumentException, IllegalAccessException;
T delete(Class<T> class1, Object primaryKey) throws Exception;

void createTable(Class<T> clss);
<U>List<U> join(Object t, Class<U> classU);

//Map genericJoin(Class<?> classT, Class<?> classU);
Node genericJoin(Class<?>... classes);

void searchAndUpdateDependency(T t);
void updateReverseDependency(T t);
List<T> importFromCSV(Class<T> clss,String filePath);

List<T> importFromArray(Class<T> clss,String[][] array);
public Stack<List<Wrapper>> returnList();
List<Map<String, Object>> structuredData(JoinType joinType, Class<?>... clses);
public List<Wrapper> returnSingleEntityObjects(Class<?> clss);
@Deprecated
public EntityManager<T> link(Class<?> clss);
public EntityManager<T> filter(Predicate<T> predicate);
public List<T> find(Query<T> query);
public List<JoinedRow> findWithJoin(Query<T> query);
public List<JoinedRow> returnJoinedRowAsPerQueryPlan(QueryPlan plan);
List<T> buildWithQuery(String sql, List<Object> params);
public List<T> buildInstances(Class<T> clss);
// connect is the evolved method of link with new functionality to describe join
public <U>EntityManager<U> connect(Class<U> clss, JoinType NAME);
public <U extends Comparable<U>> MetricsManager<U> getMetrics(Class<U> clss, String ColumnName);
<U extends Comparable<U>,K> GroupMetricsManager<U,T> getGroupMetrics(Class<U> clss, String ColumnName, String methodName);
// facility to provide predicate if required only a particular no of records based on the filter
<U extends Comparable<U>, K> GroupMetricsManager<U, T> getGroupMetrics(Class<U> clss, String ColumnName, String methodName, Predicate<T> predicate);
public EntityManager<T> apply(Consumer<T> con);

}
