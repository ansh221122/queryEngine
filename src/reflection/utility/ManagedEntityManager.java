package reflection.utility;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import objectPoolPattern.pool.ObjectPool;
import query.tree.Executor;
import query.tree.InMemoryExecutor;
import query.tree.JoinedRow;
import query.tree.Query;
import query.tree.QueryPlan;
import query.tree.SelectFieldMeta;
import query.tree.SqlExecutor;
import util.ColumnField;
import util.GroupMetricsManager;
import util.Metamodel;
import util.MetamodelStructure;
import util.MetricsManager;
import util.XMLMetamodel;

public class ManagedEntityManager<T> implements EntityManager<T>{


	@Inject(classname="")
	protected ObjectPool<Connection> connection;
	
	@Inject(classname="")
	protected Connection conn;
	
	protected Class<T> clss;
	
	@uniqueIdUtil(uniqueIdUtilClass = RandomIntegerFactory.class)
	IntegerFactory integerFactory=null;

	public ManagedEntityManager() {
		super();
		// TODO Auto-generated constructor stub
	}

//	public ManagedEntityManager(Class<?> t) {
//		super();
//		
//		
//		
//		//Class<T> t =null ;
//		long max=checkMaxPrimaryKeyValue(t);
//		// TODO Auto-generated constructor stub
//	}
	@Override
	public void updateReverseDependency(T t) {
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}
	
	public <U extends Comparable<U>, K> GroupMetricsManager<U, T> getGroupMetrics(Class<U> clss, String ColumnName, String methodName, Predicate<T> predicate){
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}

	private Long checkMaxPrimaryKeyValue(Metamodel metamodel) {
		Long max=0L;
		String sql= metamodel.checkForPrimaryKeyFieldMaxValueQuery();
		try(PreparedStatement statement=prepareStatementWith(sql).getPreparedStatement();
		ResultSet resultSet=statement.executeQuery();)
		{
			if(resultSet.next())
			{
				max=resultSet.getLong(metamodel.getPrimaryKey().getName());
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return max;
	}

	@Override
	public EntityManager<T> apply(Consumer<T> con)
	{
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}
	
	@Override
	public List<T> pivotColumn(Class<T> clss, String pivotColumnName) throws Exception
	{
		
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}

	@Override
	public List<T> pivotColumn(Class<T> clss, String[] pivotColumnName) throws Exception
	{
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}
	
	@Override
	public void persist(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		Metamodel metamodel=Metamodel.of(t.getClass());
		try
		{
		boolean ifExists=metamodel.getAnnotationMap().get(unique.class)!=null?inspectUnique(t):false;
		if(ifExists)
		{
		updateBasedOnUnique(t);
			return;
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
				//idGenerator = new AtomicLong();
		searchAndUpdateDependency(t);
		persistFinalObject(t);
	}
	
	public void persistFinalObject(T t)  {
		Metamodel metamodel=Metamodel.of(t.getClass());
		
		String sql=metamodel.buildInsertQuery();
		
		try(PreparedStatement statement = prepareStatementWith(sql).andParameters(t);)
		{
			statement.executeUpdate();
		
			
		} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		Metamodel metamodel=Metamodel.of(t.getClass());
		//idGenerator = new AtomicLong();
		String sql=metamodel.buildUpdateQuery();
		
		try(PreparedStatement statement = prepareStatementWith(sql).andUpdateParameters(t);)
		{
			statement.executeUpdate();
		
			
		}
	}
	
	
	public void updateBasedOnUnique(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		Metamodel metamodel=Metamodel.of(t.getClass());
		//idGenerator = new AtomicLong();
		List<ColumnField> uniqueFields = metamodel.getAnnotationMap().get(unique.class);
		String sql=metamodel.buildUpdateQueryGenericColumnFields(uniqueFields);
		System.out.println("update sql----->"+sql);
		try(PreparedStatement statement = prepareStatementWith(sql).andParametersWrapperList(t, metamodel.getColumns(), 1).andParametersWrapperList(t, uniqueFields, metamodel.getColumns().size()+1).getPreparedStatement();)
		{
			statement.executeUpdate();
		
			
		}
	}
	
	public void update(T t,String columnName) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		Metamodel metamodel=Metamodel.of(t.getClass());
		ColumnField fld=metamodel.getNameFieldMapping().get(columnName);
		if(fld==null)
		{
			System.out.println("Field not found.");
			return;
		}
		//idGenerator = new AtomicLong();
		String sql=metamodel.buildUpdateQueryGenericColumnFilter(columnName);
		fld.getField().setAccessible(true);
		ColumnField[] cf=new ColumnField[metamodel.getColumns().size()];
		for(int i=0;i<metamodel.getColumns().size();i++)
		{
			cf[i]=metamodel.getColumns().get(i);
		}
		try(PreparedStatement statement = prepareStatementWith(sql).andParametersWrapper(t, cf).andQueryKey(fld.getField().get(t),metamodel.getColumns().size()+1);)
		{
			statement.executeUpdate();
		}
	}

	protected PreparedStatementWrapper prepareStatementWith(String sql) throws SQLException {
				
		Connection con = null;
		System.out.println(conn);
		System.out.println(connection);
		
		if(connection !=null)
				con = connection.borrowObject();
		else
			con = conn;
				PreparedStatement preparedStatement= con.prepareStatement(sql);
				connection.returnObject(con);
				return new PreparedStatementWrapper(preparedStatement);
				
				
				}


	//private AtomicLong idGenerator ;
	public class PreparedStatementWrapper
	{
		//private PreparedStatementWrapper psw=null;

		
		

		protected PreparedStatement preparedStatement;

		public PreparedStatement getPreparedStatement() {
			return preparedStatement;
		}

		public void setPreparedStatement(PreparedStatement preparedStatement) {
			this.preparedStatement = preparedStatement;
		}

		public PreparedStatementWrapper(PreparedStatement preparedStatement) {
			// TODO Auto-generated constructor stub
			this.preparedStatement=preparedStatement;
		}

		

		
		public PreparedStatement andParameters(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
			// TODO Auto-generated method stub
			
			Metamodel metamodel=Metamodel.of(t.getClass());
			Class<?> primaryKeyType= metamodel.getPrimaryKey().getType();
			if(primaryKeyType == long.class)
			{
				long id = 0;
				if(integerFactory== null)
				{
					id=checkMaxPrimaryKeyValue(metamodel);
					id+=1;
					System.out.println("id------> "+id);
					
				}else
				{
					id = integerFactory.returnRandomInteger();
				}
				preparedStatement.setLong(1, (id));
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
				}else if(columnFieldType==long.class)
				{
					preparedStatement.setLong(columnIndex+2, (long)value);
				}
				else if(columnFieldType.getSuperclass()==Object.class)
				{
					preparedStatement.setObject(columnIndex+2, value);
				}
					
				
				
			}
			return preparedStatement;
		}

		public PreparedStatement andUpdateParameters(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
			// TODO Auto-generated method stub
			
			Metamodel metamodel=Metamodel.of(t.getClass());
			Class<?> primaryKeyType= metamodel.getPrimaryKey().getType();
			System.out.println("primary key -->"+primaryKeyType);
			int idIndex=0;
			for(int columnIndex=0;columnIndex<metamodel.getColumns().size();columnIndex++)
			{
				ColumnField columnField=metamodel.getColumns().get(columnIndex);
				Class<?> columnFieldType=columnField.getType();
				Field field=columnField.getField();
				field.setAccessible(true);
				Object value=field.get(t);
				if(columnFieldType==int.class)
				{
					preparedStatement.setInt(columnIndex+1, (int)value);
				}else if(columnFieldType==String.class)
				{
					preparedStatement.setString(columnIndex+1, (String)value);
				}else if(columnFieldType==long.class)
				{
					preparedStatement.setLong(columnIndex+1, (long)value);
				}else if(columnFieldType.getSuperclass()==Object.class)
				{
					preparedStatement.setObject(columnIndex+1, value);
				}
				
				idIndex=columnIndex;
			}
			
			
			
			if(primaryKeyType == long.class)
			{

				//System.out.println("id------> "+id);
				
				Field field=metamodel.getPrimaryKey().getField();
				field.setAccessible(true);
				Object id=field.get(t);
				preparedStatement.setLong(idIndex+2,(Long) id);
				//field.setAccessible(true);
				//field.set(t,id);
			
			}
			
			return preparedStatement;
		}
		
		
		
		public PreparedStatement andParameters(T t, ColumnField[] columns) throws SQLException, IllegalArgumentException, IllegalAccessException {
			// TODO Auto-generated method stub
			

			int idIndex=0;
			for(int columnIndex=0;columnIndex<columns.length;columnIndex++)
			{
				ColumnField columnField=columns[columnIndex];
				Class<?> columnFieldType=columnField.getType();
				Field field=columnField.getField();
				field.setAccessible(true);
				Object value=field.get(t);
				if(columnFieldType==int.class)
				{
					preparedStatement.setInt(columnIndex+1, (int)value);
				}else if(columnFieldType==String.class)
				{
					preparedStatement.setString(columnIndex+1, (String)value);
				}else if(columnFieldType==long.class)
				{
					preparedStatement.setLong(columnIndex+1, (long)value);
				}
				else if(columnFieldType.getSuperclass()==Object.class)
				{
					preparedStatement.setObject(columnIndex+1, value);
				}
				
				idIndex=columnIndex;
			}
			
			
			

			
			return preparedStatement;
		}
		
		
		
		
		public PreparedStatementWrapper andParametersWrapper(T t, ColumnField[] columns) throws SQLException, IllegalArgumentException, IllegalAccessException {
			// TODO Auto-generated method stub
			
			Metamodel metamodel=Metamodel.of(t.getClass());
			Class<?> primaryKeyType= metamodel.getPrimaryKey().getType();
			System.out.println("primary key -->"+primaryKeyType);
			int idIndex=0;
			for(int columnIndex=0;columnIndex<columns.length;columnIndex++)
			{
				ColumnField columnField=columns[columnIndex];
				Class<?> columnFieldType=columnField.getType();
				Field field=columnField.getField();
				field.setAccessible(true);
				Object value=field.get(t);
				if(columnFieldType==int.class)
				{
					preparedStatement.setInt(columnIndex+1, (int)value);
				}else if(columnFieldType==String.class)
				{
					preparedStatement.setString(columnIndex+1, (String)value);
				}else if(columnFieldType==long.class)
				{
					preparedStatement.setLong(columnIndex+1, (long)value);
				}else if(columnFieldType.getSuperclass()==Object.class)
				{
					preparedStatement.setObject(columnIndex+1, value);
				}
				
				idIndex=columnIndex;
			}
			
			
			

			
			return this;
		}
		
		public PreparedStatementWrapper andQueryParameters(List<Object> params) throws SQLException, IllegalArgumentException, IllegalAccessException {
			// TODO Auto-generated method stub\
			for(int i=0,j=1;i<params.size();i++,j++)
			{
				Object obj = params.get(i);
				if(obj.getClass()==Integer.class || obj.getClass()==int.class)
				{
					preparedStatement.setInt(j, (int)obj);
				}else if(obj.getClass()==String.class)
				{
					preparedStatement.setString(j, (String)obj);
				}else if(obj.getClass()==long.class || obj.getClass() == Long.class)
				{
					preparedStatement.setLong(j, (long)obj);
				}else if(obj.getClass()==double.class || obj.getClass() == Double.class)
				{
					preparedStatement.setDouble(j, (double)obj);
				}else if(obj.getClass().getSuperclass()==Object.class)
				{
					preparedStatement.setObject(j, obj);
				}
				
			}
			
			
			

			
			return this;
		}
		
		
		
		public PreparedStatementWrapper andParametersWrapperList(T t, List<ColumnField> columns, int index) throws SQLException, IllegalArgumentException, IllegalAccessException {
			// TODO Auto-generated method stub
			if(index==0)
				index =1;
			Metamodel metamodel=Metamodel.of(t.getClass());
			Class<?> primaryKeyType= metamodel.getPrimaryKey().getType();
			System.out.println("primary key -->"+primaryKeyType);
			
			for(int columnIndex=0;columnIndex<columns.size();columnIndex++,index++)
			{
				ColumnField columnField=columns.get(columnIndex);
				Class<?> columnFieldType=columnField.getType();
				Field field=columnField.getField();
				field.setAccessible(true);
				Object value=field.get(t);
				if(columnFieldType==int.class)
				{
					preparedStatement.setInt(index, (int)value);
				}else if(columnFieldType==String.class)
				{
					preparedStatement.setString(index, (String)value);
				}else if(columnFieldType==long.class)
				{
					preparedStatement.setLong(index, (long)value);
				}
				
				else if(columnFieldType.getSuperclass()==Object.class)
				{
					preparedStatement.setObject(index, value);
				}
				
				
				
			}
			
			
			

			
			return this;
		}

		public PreparedStatement andPrimaryKeys(Object primaryKey) throws SQLException {
			// TODO Auto-generated method stub
						
			if(primaryKey.getClass()==Long.class)
			{
				preparedStatement.setLong(1, (Long)primaryKey);
			}
			return preparedStatement;
		}
		public PreparedStatement andQueryKey(Object queryKey) throws SQLException
		{
			return andQueryKey(queryKey, 0);
		}
		public PreparedStatement andQueryKey(Object queryKey,int position) throws SQLException {
			// TODO Auto-generated method stub
			if( position == 0)
				position=1;
			if(queryKey.getClass()==Long.class)
			{
				preparedStatement.setLong(position, (Long)queryKey);
			}
			else if(queryKey.getClass()==int.class)
			{
				preparedStatement.setInt(position, (int)queryKey);
			}
			else if(queryKey.getClass()==String.class)
			{
				preparedStatement.setString(position, (String)queryKey);
			}
			else if(queryKey.getClass().getSuperclass()==Object.class)
			{
				preparedStatement.setObject(position, queryKey);
			}
			
			return preparedStatement;
		}
		
	}
	
	
	@Override
	public List<T> importFromArray(Class<T> clss, String[][] array) {
    	//File file = new File(filePath);
    	String[] columnNames=null;
    	String[] data;
    	List<T> returnList = new ArrayList<>();
    	List<String> columnValues = null;
    	if(array.length==0 || array[0].length==0)
    		return null;

    	try {
			
			String line="";
			int i=0;
			while(i<array.length)
			{
				if(i==0)
				{
					columnNames=array[i];
					columnValues=Arrays.stream(columnNames).map((s)->s.replaceAll("^\"|\"$", "")).collect(Collectors.toList());
					
				}
				else
				{
					
					data=array[i];
					List<String> dataValues=Arrays.stream(data).map((s)->s.replaceAll("^\"|\"$", "")).collect(Collectors.toList());
					Map<String,String> keyValueMap=new HashMap();
					T t=null;
					if(data.length!=columnNames.length)
						continue;
					for(int j=0;j<dataValues.size();j++)
					{
						keyValueMap.put((String) columnValues.get(j), dataValues.get(j));
						 t = buildInstance(clss, keyValueMap);

						
						
					}
					if(t!=null)
					{
					persist(t);
					returnList.add(t);					
					}
					
					
				}
				i++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    				
    	return returnList;
    }

    @Override
    public List<T> importFromCSV(Class<T> clss,String filePath) {
    	File file = new File(filePath);
    	String[] columnNames;
    	String[] data;
    	List<T> returnList = new ArrayList<>();
    	List<String> columnValues = null;
    	if(!file.exists())
    		return null;
    	if(!file.getName().toLowerCase().contains(".csv"))
    		return null;
    	try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line="";
			int i=0;
			while((line=reader.readLine())!=null)
			{
				if(i==0)
				{
					columnNames=line.split(",");
					columnValues=Arrays.stream(columnNames).map((s)->s.replaceAll("^\"|\"$", "")).collect(Collectors.toList());
					
				}
				else
				{
					
					data=line.split(",");
					List<String> dataValues=Arrays.stream(data).map((s)->s.replaceAll("^\"|\"$", "")).collect(Collectors.toList());
					Map<String,String> keyValueMap=new HashMap();
					T t=null;
					for(int j=0;j<dataValues.size();j++)
					{
						keyValueMap.put((String) columnValues.get(j), dataValues.get(j));
						 t = buildInstance(clss, keyValueMap);

						
						
					}
					if(t!=null)
					{
					persist(t);
					returnList.add(t);					
					}
					
					
				}
				i++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    				
    	return returnList;
    }
	
	
	protected T buildInstance(Class<T> clss, Map<String,String> resultSet) throws Exception {
		T t =clss.getConstructor().newInstance();
		Metamodel metamodel=Metamodel.of(clss);
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		//Iterator<String> iterator = resultSet.keySet().iterator();
		
		for(ColumnField columnField:metamodel.getColumns())
		{
			try {
			Field field = columnField.getField();
			String value = resultSet.get(columnField.getName());
			if(value !=null)
			{
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				
				if(columnType==int.class)
				{
					int val=Integer.valueOf(value);
					field.set(t, val);
				}else if(columnType==String.class)
				{
					
					field.set(t, value);
				}else if(columnType==long.class)
				{
					Long val=Long.valueOf(value);
					field.set(t, val);
				}
				else if(columnType==double.class)
				{
					Double val=Double.valueOf(value);
					field.set(t, val);
				}
				else if(columnType.getSuperclass()== Object.class)
				{
					// prepare map from value in the format eg. city:colorado;zipcode:54321 then reiterate the object in buildInstance method for generic class
					if(value.contains(";"))
					{
						String[] value_pair = value.split(";");
						HashMap<String, String> column_to_value_map = new HashMap<>();
						for(String field_value: value_pair )
						{
							String fieldName = field_value.split(":")[0].trim();
							String fieldValue = field_value.split(":").length>1?field_value.split(":")[1].trim():field_value.split(":")[0].trim();
							column_to_value_map.put(fieldName, fieldValue);
						}
						
						
						Object valueObject=null;
						
						if(Metamodel.of(columnType).getPrimaryKey()==null)
						valueObject= buildInstance(columnType, column_to_value_map,columnType.getDeclaredFields());
						else
							valueObject= buildInstance(columnType, column_to_value_map);
						System.out.println("<><><><><><>"+valueObject);
						field.set(t, valueObject);
					}
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
			
		}
		
		
		List<Field> injectFields = metamodel.getInjectList();
		injectFields.stream().forEach((field)->{
	
			try {
			
			Class<?> injectclss = getGenericClassRuntime(field);
			field.setAccessible(true);
			field.set(t, join(t,injectclss));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		});
		
		return t;
	}
	
	
	protected Object buildInstance(Class<?> clss, HashMap<String,String> resultSet) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		Object t =clss.getConstructor().newInstance();
		Metamodel metamodel=Metamodel.of(clss);
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		//Iterator<String> iterator = resultSet.keySet().iterator();
		
		for(ColumnField columnField:metamodel.getColumns())
		{
			try {
			Field field = columnField.getField();
			String value = resultSet.get(columnField.getName());
			if(value !=null)
			{
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				
				if(columnType==int.class)
				{
					int val=Integer.valueOf(value);
					field.set(t, val);
				}else if(columnType==String.class)
				{
					
					field.set(t, value);
				}else if(columnType==long.class)
				{
					Long val=Long.valueOf(value);
					field.set(t, val);
				}
				else if(columnType.getSuperclass()== Object.class)
				{
					// prepare map from value in the format eg. city:colorado;zipcode:54321 then reiterate the object in buildInstance method for generic class
					if(value.contains(";"))
					{
						String[] value_pair = value.split(";");
						HashMap<String, String> column_to_value_map = new HashMap<>();
						for(String field_value: value_pair )
						{
							String fieldName = field_value.split(":")[0].trim();
							String fieldValue = field_value.split(":").length>1?field_value.split(":")[1].trim():field_value.split(":")[0].trim();
							column_to_value_map.put(fieldName, fieldValue);
						}
						
						
						Object valueObject=null;
						
						if(Metamodel.of(columnType).getPrimaryKey()==null)
						valueObject= buildInstance(columnType, column_to_value_map,columnType.getDeclaredFields());
						else
							valueObject= buildInstance(columnType, column_to_value_map);
						System.out.println("<><><><><><>"+valueObject);
						field.set(t, valueObject);
					}
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
			
		}
		
		
		List<Field> injectFields = metamodel.getInjectList();
		injectFields.stream().forEach((field)->{
	
			try {
			
			Class<?> injectclss = getGenericClassRuntime(field);
			field.setAccessible(true);
			field.set(t, join(t,injectclss));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		});
		
		return t;
	}
	
	
	
	protected Class<?> getGenericClassRuntime(Field field)
	{
		
		 try
	        {
	            //System.out.println("Field " + field.getName() + " is of type " + field.getType().getSimpleName());

	            Type genericType = field.getGenericType();

	            if(genericType instanceof ParameterizedType)
	            {
	                ParameterizedType type = (ParameterizedType) genericType;               
	                Type[] typeArguments = type.getActualTypeArguments();
	                return (Class<?>) typeArguments[0];
	                
	            }
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
		 
		 return null;
		
	}
	
	protected Object buildInstance(Class<?> clss, Map<String,String> resultSet, Field[] fields) throws Exception {

		Object t =clss.getConstructor().newInstance();


		

		for(Field columnField:fields)
		{
			try {
			Field field = columnField;
			String value = resultSet.get(columnField.getName());
			if(value !=null)
			{
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				
				if(columnType==int.class)
				{
					int val=Integer.valueOf(value);
					field.set(t, val);
				}else if(columnType==String.class)
				{
					
					field.set(t, value);
				}else if(columnType==long.class)
				{
					Long val=Long.valueOf(value);
					field.set(t, val);
				}

			}
			}catch(Exception e)
			{
				System.out.println("value for columnName::::"+columnField.getName()+" not set due to some exception.");
			}
		}

		return t;
	}
	
	
	
	@Override
	public T delete(Class<T> clss, Object primaryKey) throws Exception {
		// TODO Auto-generated method stub
		Metamodel metamodel=Metamodel.of(clss);
		String sql =metamodel.buildDeletionRequest();
		T t= find(clss ,primaryKey);
		try(PreparedStatement statement=prepareStatementWith(sql).andPrimaryKeys(primaryKey);
		)
		{
			statement.executeUpdate();
		
		}
		return t;
	}
	
	@Override
	public T find(Class<T> clss, Object primaryKey) throws Exception {
		Metamodel metamodel=Metamodel.of(clss);
		String sql =metamodel.buildSelectRequest();
		try(PreparedStatement statement=prepareStatementWith(sql).andPrimaryKeys(primaryKey);
		ResultSet resultSet=statement.executeQuery();)
		{
		return buildInstance(clss, resultSet,metamodel);
		}
		
	}
	
	public List<T> buildWithQuery(String sql, List<Object> params) {
		Metamodel metamodel=Metamodel.of(clss);
		try(PreparedStatement statement=prepareStatementWith(sql).andQueryParameters(params).getPreparedStatement();
		ResultSet resultSet=statement.executeQuery();)
		{
		return buildInstances(clss, resultSet,metamodel);
		}catch(Exception e){
			throw new RuntimeException("Query failed with exception: "+e);
		}
		
	}
	
	protected Object transform(Class<?> cls, ResultSet rs, String name)
	{
		
		Object val=null;
		try {
			String[] classNameHierarchy = cls.getName().split("\\.");
			
			String className = classNameHierarchy.length!=0
										?classNameHierarchy[classNameHierarchy.length-1]
										:cls.getName();
		switch(className)
		{
		case "int":
			val= Integer.valueOf(rs.getInt(name));
		break;
		case "Integer":
			val= Integer.valueOf(rs.getInt(name));
			break;
		case "String":
			val = String.valueOf(rs.getString(name));
			break;
		case "Double":
			val= Double.valueOf(rs.getDouble(name));
			break;
		case "double":
			val= Double.valueOf(rs.getDouble(name));
			break;
		case "long":
			val = Long.valueOf(rs.getLong(name));
			break;
		case "Long":
			val = Long.valueOf(rs.getLong(name));
			break;
		default:
			if(cls.getSuperclass()==Object.class)
			{
				byte[] value = rs.getBytes(name);
			    if(value!=null)
			    {
					ByteArrayInputStream in = new ByteArrayInputStream(value);
				    ObjectInputStream is = new ObjectInputStream(in);
				    val= is.readObject();
				    try {
				    	is.close();
				    	in.close();
				    }
				    catch(Exception e)
				    {
				    	e.printStackTrace();
				    }
			    }
			    //System.out.println(">>>>>>>>>>>>>>"+value);
				
			}
		
			
		}
		
		}
		
		catch(Exception e)
		{e.printStackTrace();
			//return null;
		}
		
		
	return val;
	}
	
	
	private T buildInstance(Class<T> clss, ResultSet resultSet, Metamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
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
				Field field= columnField.getField();
				field.set(t, transform(columnField.getType(),resultSet,columnField.getName()));
				
			}
			
			
			updateInjectListFields(metamodel, t);
		
		return t;
	}
	
	@Override
	public List<JoinedRow> returnJoinedRowAsPerQueryPlan(QueryPlan plan) {
		// TODO Auto-generated method stub
		List<JoinedRow> joinedRows = new ArrayList<>();
		try(PreparedStatement statement=prepareStatementWith(plan.getSql()).andQueryParameters(plan.getParams()).getPreparedStatement();
		ResultSet resultSet=statement.executeQuery();)
		{
			while(resultSet.next()) {
				Map<Class<?>, Object> instances = returnInstanceMapFromSelectFieldMeta(plan.getFieldMappings(), resultSet);
				JoinedRow row = new JoinedRow();
				instances.forEach(row::put);
				joinedRows.add(row);
				
			}
			
		}catch(Exception e){
			throw new RuntimeException("Query failed with exception: "+e);
		}
		
		return joinedRows;
	}
	
	
	private Object createInstance(Class<?> clss) {
		try {
			return clss.getConstructor().newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error in instance creation for class: "+clss);
		}
	}
	
	private Map<Class<?>, Object> returnInstanceMapFromSelectFieldMeta(List<SelectFieldMeta> fieldMappings, ResultSet rs ){
		
		Map<Class<?>, Object> instances =
			    new HashMap<>();
		
		for (SelectFieldMeta meta : fieldMappings) {
		    try {
				Object entity =
			        instances.computeIfAbsent(
			            meta.getEntityClass(),
			            cls -> createInstance(cls)
			        );
			    Metamodel metamodel = Metamodel.of(meta.getEntityClass());
			    Map<String, Field> fieldMap = metamodel.getAllFieldMapping();
			    Field field = fieldMap.get(meta.getFieldName().split("\\.")[1]);
			    Object value =
			        transform(field.getType(), rs, meta.getColumnAlias());
			    field.setAccessible(true);
			    field.set(entity, value);
		    }catch(Exception e) {
		    	e.printStackTrace();
		    }
		
		}
		
		
		return instances;
	}
	
	private List<T> buildInstances(Class<T> clss, ResultSet resultSet, Metamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		
		List<T> instanceList = new ArrayList<>();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		try {
			while(resultSet.next()) {
				
				T t =clss.getConstructor().newInstance();
				if(primaryKeyType==long.class)
				{
					long primaryKey=resultSet.getInt(primaryKeyFieldName);
					primaryKeyField.setAccessible(true);
					primaryKeyField.set(t, primaryKey);
					
				}
				for (ColumnField columnField : metamodel.getColumns()) {
					Field field= columnField.getField();
					field.setAccessible(true);
					field.set(t, transform(columnField.getType(),resultSet,columnField.getName()));
					
				}
				
				
				updateInjectListFields(metamodel, t);
				instanceList.add(t);
				
			}
		}catch(Exception e) {
			
			throw new RuntimeException("Exception in build instance:"+e);
		}finally {
			resultSet.close();
		}
		return instanceList;
	}

	protected void updateInjectListFields(MetamodelStructure metamodel, Object t) {
		List<Field> injectFields = metamodel.getInjectList();
		//System.out.println(injectFields);
		injectFields.stream().forEach((field)->{

			try {
			
			Class<?> injectclss = getGenericClassRuntime(field);
			field.setAccessible(true);
			field.set(t, join(t,injectclss));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		});
	}

	@Override
	public void createTable(Class<T> clss) {
		// TODO Auto-generated method stub
		
		Metamodel metamodel=Metamodel.of(clss);
		String sql =metamodel.buildCreateQuery();
		
		try(PreparedStatement create=prepareStatementWith(sql).getPreparedStatement()){
		System.out.println("query-->"+sql);
			create.execute();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Table already exists"+":"+e.getMessage());
		}
	}

	@Override
	public <U>List<U> join(Object t, Class<U> classU) {
		// TODO Auto-generated method stub
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}

//	@Override
//	public Map genericJoin(Class<?> classT, Class<?> classU) {
//		// TODO Auto-generated method stub
//		System.out.println("This functionality was added in relationJoinManager release version");
//		return null;
//	}

	@Override
	public void searchAndUpdateDependency(T t) {
		// TODO Auto-generated method stub
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
		
	}

	@Override
	public boolean inspectUnique(T t) throws Exception {
		// TODO Auto-generated method stub
		
		Metamodel metamodel=Metamodel.of(t.getClass());
		Map<Class<?>,List<ColumnField>> annotationMap=metamodel.getAnnotationMap();
		String sql="";
		ColumnField[] uniqueFields=null;
		try
		{
			System.out.println(annotationMap);
			
			List<ColumnField> uniqueList=annotationMap.get(unique.class);
			uniqueFields=new ColumnField[uniqueList.size()];
			int i=0;
			for(ColumnField field:uniqueList)
			{
				//System.out.println("Field<><><><><><>--->"+field.getField().getName());
				uniqueFields[i++]=field;
			}
			sql=metamodel.buildSelectRequest(uniqueFields);
			System.out.println("query to check unique::::"+sql);
			//ColumnField[] uniqueFields=uniqueFieldList;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
				//idGenerator = new AtomicLong();
		//String sql=metamodel.buildSelectRequest(un);
		
		try(PreparedStatement statement = prepareStatementWith(sql).andParameters(t,uniqueFields);ResultSet rs=statement.executeQuery())
		{
			boolean ifExists= rs.next();
			if(ifExists)
			{
			Field field = metamodel.getPrimaryKey().getField();
			field.setAccessible(true);
			Class<?> fieldType = field.getType();
			if(fieldType==Long.class || fieldType==long.class)
				field.set(t, rs.getLong(metamodel.getPrimaryKey().getName()));
			else
				field.set(t, rs.getInt(metamodel.getPrimaryKey().getName()));
			}
			return ifExists;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		return false;
		}
		}

	@Override
	public Node genericJoin(Class<?>... classes) {
		// TODO Auto-generated method stub
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}

	@Override
	public List find(Class<T> clss, String columnName, Object columnValue) throws Exception {
		// TODO Auto-generated method stub
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
		
	}


	
	@Override
	public List<T> find(Class<T> class1, Predicate<T> predicate) throws Exception {
		// TODO Auto-generated method stub
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}
	
	public List<T> findAndProcess(Class<T> class1, Predicate<T> predicate, Function<T, T> function) throws Exception {
		/**
		 * this method is used to find object list supplying a predicate condition 
		 * then processing using a user defined
		 * function.
		 * 
		 * 
		 * **/

		
		List<T> list=find(class1, predicate);
		// process each element from list using(user defined method) that passed the predicate test
		List<T> returnList = list
				.stream()
				.map((t)->function.apply(t))
				.collect(Collectors.toList());
		return returnList;
	}

	@Override
	public Stack<List<Wrapper>> returnList() {
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
		
			}

	@Override
	public EntityManager<T> link(Class<?> clss) {
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
		
		
	}

	@Override
	public EntityManager<T> filter(Predicate<T> predicate) {
		// TODO Auto-generated method stub
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
		
	}

	@Override
	public List<Wrapper> returnSingleEntityObjects(Class<?> clss) {
		// TODO Auto-generated method stub
		throw new RuntimeException("This functionality was added in relationJoinManager release version");
	}

	@Override
	public <U> EntityManager<U> connect(Class<U> clss, JoinType NAME) {
		// TODO Auto-generated method stub
		System.out.println("This functionality was added in relationJoinManager release version");
		return null;
	}

	@Override
	public <U extends Comparable<U>> MetricsManager<U> getMetrics(Class<U> clss, String ColumnName) {
		Metamodel metamodel = Metamodel.of(clss);
		Map<String,ColumnField> fieldMap = metamodel.getNameFieldMapping();
		ColumnField field = fieldMap.get(ColumnName);
		if(field == null)
		{
			throw new RuntimeException("Field not present, check proper case as well");
		}
		
		Map<Class<?>,List<ColumnField>> annotationMap = metamodel.getAnnotationMap();
		List<ColumnField> metricFields = annotationMap.get(metric.class);
		if(!metricFields.contains(field))
		{
			throw new RuntimeException("Metric annotation not applied.");
		}
		
		
		Class<?> fieldClass =  field.getField().getType();
		if(fieldClass!=clss) 
		{
			throw new RuntimeException("Field not matching.");
		}
		
		
		
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
		throw new RuntimeException("method added in future version(RelationJoinManager).");
	}

	@Override
	public List<T> find(Query<T> query) {
		Executor executor = new SqlExecutor(Metamodel.of(clss), this);	
		return executor.execute(query, null);
	}

	@Override
	public List<T> buildInstances(Class<T> clss) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JoinedRow> findWithJoin(Query<T> query) {
		Executor executor = new SqlExecutor(Metamodel.of(clss), this);	
		return executor.executeWithJoin(query, null);
	}









	
}
