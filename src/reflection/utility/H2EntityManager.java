package reflection.utility;

import java.sql.Connection;
import java.sql.DriverManager;
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
import util.XMLMetamodel;

@Deprecated
public class H2EntityManager<T> extends AbstractEntityManager<T> {
	public Connection buildConnection() throws SQLException {
		Connection connection = DriverManager.getConnection("jdbc:h2:D:\\Work\\Projects\\eclipse-workspace\\ImageBrowser\\db_files\\db-pluralsight", "sa", "");
		return connection;
	}
	
	public void updateReverseDependency(T t) {
		
	}
	
	@Override
	public void updateBasedOnUnique(T t) throws SQLException, IllegalArgumentException, IllegalAccessException
	{
		System.out.println("This Feature Not supported in this class.");
	}
	
	
	
	public <U extends Comparable<U>, K> GroupMetricsManager<U, T> getGroupMetrics(Class<U> clss, String ColumnName, String methodName, Predicate<T> predicate){
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}
	
	public List<T> pivotColumn(Class<T> clss, String pivotColumnName) throws Exception
	{
		return null;
	}
	public List<T> pivotColumn(Class<T> clss, String[] pivotColumnName) throws Exception
	{
		return null;
	}

	@Override
	public void createTable(Class<T> clss) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EntityManager<T> apply(Consumer<T> con)
	{
		System.out.println("This functionality was added in relationJoinManager release version");
		return null;
	}

	@Override
	public void update(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <U>List<U> join(Object t, Class<U> classU) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T delete(Class<T> class1, Object primaryKey) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


//
//	@Override
//	public Map genericJoin(Class<?> classT, Class<?> classU) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void searchAndUpdateDependency(T t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean inspectUnique(T t) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Node genericJoin(Class<?>... classes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List find(Class<T> class1, String columnName, Object columnValue) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public void update(T t,String columnName) throws SQLException, IllegalArgumentException, IllegalAccessException
	{
		
	}

	@Override
	public List importFromCSV(Class<T> clss, String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> importFromArray(Class<T> clss, String[][] array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> find(Class<T> class1, Predicate<T> predicate) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stack<List<Wrapper>> returnList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager<T> link(Class<?> clss) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager<T> filter(Predicate predicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Wrapper> returnSingleEntityObjects(Class<?> clss) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> EntityManager<U> connect(Class<U> clss,JoinType NAME) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<U>> MetricsManager<U> getMetrics(Class<U> clss, String ColumnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<U>, K> GroupMetricsManager<U, T> getGroupMetrics(Class<U> clss, String ColumnName,
			String methodName) {
		// TODO Auto-generated method stub
		throw new RuntimeException("method added in future version(RelationJoinManager).");
	}

	@Override
	public List<Map<String, Object>> structuredData(JoinType joinType, Class<?>... clses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> findAndProcess(Class<T> class1, Predicate<T> predicate, Function<T, T> function) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> find(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> buildInstances(Class<T> clss) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<T> buildWithQuery(String sql, List<Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JoinedRow> findWithJoin(Query<T> query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JoinedRow> returnJoinedRowAsPerQueryPlan(QueryPlan plan) {
		// TODO Auto-generated method stub
		return null;
	}

}
