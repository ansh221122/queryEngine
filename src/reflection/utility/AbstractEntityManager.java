package reflection.utility;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import util.ColumnField;
import util.Metamodel;
import util.PrimaryKeyField;

public abstract class AbstractEntityManager<T> implements EntityManager<T> {

	@Override
	public void persist(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		Metamodel metamodel=Metamodel.of(t.getClass());
		String sql=metamodel.buildInsertQuery();
		
		try(PreparedStatement statement = prepareStatementWith(sql).andParameters(t);)
		{
			statement.executeUpdate();
		
			
		}
	}

	private PreparedStatementWrapper prepareStatementWith(String sql) throws SQLException {
		// TODO Auto-generated method stub
		Connection connection = buildConnection();
		
				PreparedStatement preparedStatement= connection.prepareStatement(sql);
				return new PreparedStatementWrapper(preparedStatement);
				
				
				}

	public abstract Connection buildConnection() throws SQLException;
	
	private AtomicLong idGenerator = new AtomicLong(0L);
	private class PreparedStatementWrapper
	{
		private PreparedStatementWrapper psw=null;

		
		

		private PreparedStatement preparedStatement;

		public PreparedStatementWrapper(PreparedStatement preparedStatement) {
			// TODO Auto-generated constructor stub
			this.preparedStatement=preparedStatement;
		}

		public PreparedStatement andParameters(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
			// TODO Auto-generated method stub
			
			Metamodel metamodel=Metamodel.of(t.getClass());
			Class<?> primaryKeyType= metamodel.getPrimaryKey().getType();
			System.out.println("primary key -->"+primaryKeyType);
			if(primaryKeyType == long.class)
			{
				long id=idGenerator.incrementAndGet();
				System.out.println("id------> "+id);
				preparedStatement.setLong(1, id);
				Field field=metamodel.getPrimaryKey().getField();
				field.setAccessible(true);
				field.set(t,id);
			
			} 
			for(int columnIndex=0;columnIndex<metamodel.getColumns().size();columnIndex++)
			{
				ColumnField columnField=metamodel.getColumns().get(columnIndex);
				Class<?> columnFieldType=columnField.getType();
				Field field=columnField.getField();
				field.setAccessible(true);
				Object value=field.get(t);
				if(columnFieldType==int.class)
				{
					preparedStatement.setInt(columnIndex+2, (int)value);
				}else if(columnFieldType==String.class)
				{
					preparedStatement.setString(columnIndex+2, (String)value);
				}
				
				
			}
			return preparedStatement;
		}

		public PreparedStatement andPrimaryKeys(Object primaryKey) throws SQLException {
			// TODO Auto-generated method stub
			if(primaryKey.getClass()==Long.class)
			{
				preparedStatement.setLong(1, (Long)primaryKey);
			}
			return preparedStatement;
		}
		
	}
	@Override
	public T find(Class<T> clss, Object primaryKey) throws Exception {
		Metamodel metamodel=Metamodel.of(clss);
		String sql =metamodel.buildSelectRequest();
		try(PreparedStatement statement=prepareStatementWith(sql).andPrimaryKeys(primaryKey);
		ResultSet resultSet=statement.executeQuery();)
		{
		return buildInstance(clss, resultSet);
		}
		
	}

	private T buildInstance(Class<T> clss, ResultSet resultSet) throws Exception {
		// TODO Auto-generated method stub
		Metamodel metamodel=Metamodel.of(clss);
		T t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		resultSet.next();
		if(primaryKeyType==long.class)
		{
			long primaryKey=resultSet.getInt(primaryKeyFieldName);
			primaryKeyField.setAccessible(true);
			primaryKeyField.set(t, primaryKey);
			
		}
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(columnType==int.class)
				{
					int value=resultSet.getInt(columnName);
					field.set(t, value);
				}else if(columnType==String.class)
				{
					String value=resultSet.getString(columnName);
					field.set(t, value);
				}
			}
		
		return t;
	}



}
