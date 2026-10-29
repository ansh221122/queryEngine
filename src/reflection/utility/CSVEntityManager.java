package reflection.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import query.tree.Executor;
import query.tree.InMemoryExecutor;
import query.tree.Join;
import query.tree.JoinedRow;
import query.tree.Query;
import util.CSVMetamodel;
import util.ColumnField;
import util.JsonMetamodel;
import util.Metamodel;
import util.PrimaryKeyField;
import util.XMLMetamodel;

public class CSVEntityManager<T> extends RelationJoinManager<T> {
	@Inject(classname="")
	String path;
	
	@Inject(classname="")
	char separator=',';

	public CSVEntityManager() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void updateBasedOnUnique(T t) throws SQLException, IllegalArgumentException, IllegalAccessException
	{
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
		try
		{
		boolean ifExists=metamodel.getAnnotationMap().get(unique.class)!=null?inspectUnique(t):false;
			if(ifExists)
			{
			update(t);
				return;
			}
		}
		catch(Exception e)
		{
			System.out.println("Issue in update ::::"+e);
		}
		
	}
	
	@Override
	public void persist(T t) throws SQLException, IllegalArgumentException, IllegalAccessException{
		CSVMetamodel metamodel = CSVMetamodel.of(t.getClass());
		try
		{
		boolean ifExists=metamodel.getAnnotationMap().get(unique.class)!=null?inspectUnique(t):false;
		if(ifExists)
		{
		update(t);
			return;
		}
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		File inputFile = new File(metamodel.getCSVFilePath(path));
		FileWriter fileWriter =null;
		FileReader fileReader =null;
		BufferedReader bufferedReader=null;
		try {
		if(!inputFile.exists())
		{
			createFile((Class<T>)t.getClass());
			inputFile = new File(metamodel.getCSVFilePath(path));
			

		}
		fileReader =new FileReader(inputFile);
		bufferedReader = new BufferedReader(fileReader);
		
		String outputText="";
		
		String columnNames = bufferedReader.readLine();
		outputText+=columnNames+"\n";
		String[] columnArray = columnNames.split(separator+"");
		List<String> valueArray = new ArrayList<>();
		updateUniqueID(t);
		Map<String, ColumnField> columnMap = metamodel.getNameFieldMapping();
		Field primaryKeyField=metamodel.getPrimaryKey().getField();
		primaryKeyField.setAccessible(true);
		String primaryKeyValue = primaryKeyField.get(t).toString();
		
		for(String columnName:columnArray)
		{
			if(!columnName.equals(metamodel.getPrimaryKey().getName()))
			{
				ColumnField field = columnMap.get(columnName);
				field.getField().setAccessible(true);
				Class<?> cfType=field.getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					
					File dir_class= new File(path+t.getClass().getName());
					if(!dir_class.exists())
					{
					
					dir_class.mkdir();
					}
					
					saveToFile(t, primaryKeyValue, field,path);
					valueArray.add(t.getClass().getName()+File.separator+field.getName()+"_"+primaryKeyValue);
					
				}
				else
				valueArray.add(field.getField().get(t).toString().replaceAll(separator+"", ""));
				
			}else
			{
				valueArray.add(primaryKeyValue.replaceAll(separator+"", ""));
			}
		}
		String line = null;
		while((line=bufferedReader.readLine())!=null)
		{
			outputText+=line;
			outputText+="\n";
		}
		
		outputText+=String.join(separator+"", valueArray);
		//JSONArray jsonArray = (JSONArray)(new JSONParser()).parse(fileReader);
		//updateUniqueID(t);
		//JSONObject obj = getJsonObjectFromInstance(metamodel,t);
		//jsonArray.add(obj);
		
		fileWriter = new FileWriter(inputFile);
		fileWriter.write(outputText);
		fileWriter.flush();
		//fileWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			try {
				fileReader.close();
				fileWriter.close();
				bufferedReader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void update(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		CSVMetamodel metamodel=CSVMetamodel.of(t.getClass());
		Field primaryKeyField= metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName = metamodel.getPrimaryKey().getName();
		primaryKeyField.setAccessible(true);
		Object primaryKey=primaryKeyField.get(t);
		String primaryKeyValue = primaryKey.toString();
		File file = new File(metamodel.getCSVFilePath(path));
		File inputFile = new File(metamodel.getCSVFilePath(path));
		FileWriter fileWriter =null;
		FileReader fileReader =null;
		BufferedReader bufferedReader=null;
		try {
		if(!inputFile.exists())
		{
			persist(t);
		}
		fileReader =new FileReader(inputFile);
		bufferedReader = new BufferedReader(fileReader);
		
		String outputText="";
		List<String> rows = new ArrayList<>();
		String columnNames = bufferedReader.readLine();
		outputText+=columnNames;
		Map<String, Integer> nameMap = getNameMap(columnNames);
		String line = null;
		while((line=bufferedReader.readLine())!=null)
		{
			rows.add(line);
		}
		String[][] dataset = new String[rows.size()][];
		int i=0;
		for(String row:rows)
		{
			dataset[i]=rows.get(i).split(separator+"");
			i++;
		}
		
		for(int j=0;j<dataset.length;j++)
		{
			if(dataset[j][nameMap.get(primaryKeyFieldName)].equals(primaryKey.toString()))
			{
				for(ColumnField columnField:metamodel.getColumns())
				{
					Field field = columnField.getField();//getName()
					String columnName = columnField.getName();
					field.setAccessible(true);
					Class<?> cfType = columnField.getType();
					if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
					{
						
						File dir_class= new File(path+t.getClass().getName());
						if(!dir_class.exists())
						{
						
						dir_class.mkdir();
						}
						
						saveToFile(t, primaryKeyValue, columnField,path);
						dataset[j][nameMap.get(columnName)]=t.getClass().getName()+File.separator+field.getName()+"_"+primaryKeyValue;
						
					}
					else
						dataset[j][nameMap.get(columnName)]=field.get(t).toString().replaceAll(separator+"", "");
					
					
					
					
					
				}
				break;
			}
		}
		
		
		rows = new ArrayList();
		for(int j=0;j<dataset.length;j++)
		{
			outputText+="\n";
			outputText+=String.join(separator+"", dataset[j]);
		}
		fileWriter  = new FileWriter(inputFile);
		
		fileWriter.write(outputText);
		fileWriter.flush();
		
		
		
		
		}
			catch(Exception e)
			{
				e.printStackTrace();
			}finally {
				try {
					fileReader.close();
					bufferedReader.close();
					fileWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
	
	}
	
	@Override
	public void searchAndUpdateDependency(T t) {
		super.searchAndUpdateDependency(t);
		
	}
	protected List buildInstance(Class clss) throws SQLException, Exception {
		CSVMetamodel metamodelT = CSVMetamodel.of(clss);		
		List allTElements = null;
		allTElements=buildInstance(clss, metamodelT);
		return allTElements;
	}
	
	 @Override
		protected List getEntityList(Class<?>[] clss, int index) throws SQLException, Exception {
			CSVMetamodel metamodelT = CSVMetamodel.of(clss[index]);		
			List allTElements = null;
			allTElements=buildInstance(clss[index], metamodelT);
			return allTElements;
		}
	 
	 
	 List buildInstance(Class<?> class1,CSVMetamodel metamodel ) throws Exception
	 {
		 List list = new ArrayList();
		 File file = new File(metamodel.getCSVFilePath(path));
		 FileReader fileReader =null;
			BufferedReader bufferedReader=null;
			fileReader =new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			
			String columnNames = bufferedReader.readLine();
			Map<String,Integer> nameMap=getNameMap(columnNames);
			

			System.out.println("from class csvEntityManager");
			String line =null;
			while ((line=bufferedReader.readLine())!=null)   
			{ 
				String[] row = line.split(separator+"");
				Object obj=buildInstance(class1, row, nameMap);
				updateInjectListFields(metamodel, obj);
				list.add(buildInstance(class1, row, nameMap));
			}
		return list;
	 }
	
		@Override
		public List find(Class<T> clss, String columnName, Object value) throws Exception {

			CSVMetamodel metamodel = CSVMetamodel.of(clss);
			List<Object> list = new ArrayList<>();
			File file = new File(metamodel.getCSVFilePath(path));
			Map<String, ColumnField> columnFieldMap= metamodel.getNameFieldMapping();
			ColumnField queryKey = columnFieldMap.get(columnName);
			if(!file.exists() || queryKey==null)
			{
				return null;
			}
			
			try(FileReader fileReader =new FileReader(file);
			BufferedReader bufferedReader=new BufferedReader(fileReader);)
			{

			String columnNames = bufferedReader.readLine();
			Map<String,Integer> nameMap=getNameMap(columnNames);
			if(nameMap.get(queryKey.getName())==null)
				return null;
			System.out.println("from class csvEntityManager");
			String line =null;
			while ((line=bufferedReader.readLine())!=null)   
			{ 
				String[] row = line.split(separator+"");
				
				
				if(row[nameMap.get(queryKey.getName())].equals(value.toString()))
				{
					list.add( buildInstance(clss, row, CSVMetamodel.of(clss)));
				}
				

			}
			}
			return list;
		
	    	
	    	
	    	
	    }
		
		
		
		@Override
		public List find(Class<?> clss,Object value,  String columnName ) throws Exception {

			CSVMetamodel metamodel = CSVMetamodel.of(clss);
			List<Object> list = new ArrayList<>();
			File file = new File(metamodel.getCSVFilePath(path));
			Map<String, ColumnField> columnFieldMap= metamodel.getNameFieldMapping();
			ColumnField queryKey = columnFieldMap.get(columnName);
			if(!file.exists() || queryKey==null)
			{
				return null;
			}
			
			try(FileReader fileReader =new FileReader(file);
			BufferedReader bufferedReader=new BufferedReader(fileReader);)
			{

				String columnNames = bufferedReader.readLine();
				Map<String,Integer> nameMap=new HashMap();
				
				int i=0;
				for(String columName:columnNames.split(separator+""))
				{
					nameMap.put(columName, i);
					i++;
				}
			if(nameMap.get(queryKey.getName())==null)
				return null;
			System.out.println("from class csvEntityManager");
			String line =null;
			
			while ((line=bufferedReader.readLine())!=null)   
			{ 
				String[] row = line.split(separator+"");
				
				
				if(row[nameMap.get(queryKey.getName())].equals(value.toString()))
				{
					list.add( buildInstance(clss, row, nameMap));
				}
				

			}
			}
			return list;
		
	    	
	    	
	    	
	    }
	
	
    @Override
	protected List findGeneric(Class<?> clss, ColumnField queryKey, Object value) throws Exception {
    	return findGenericMultipleConditions(clss, new ColumnField[] {queryKey}, new Object[] {value});
    } // find
    
    @Override
    public T find(Class<T> class1, Object primaryKey) throws Exception{
    	PrimaryKeyField primaryKeyField =CSVMetamodel.of(class1).getPrimaryKey();
    	return findGeneric(class1, primaryKeyField, primaryKey);
    }
	protected T findGeneric(Class<T> clss, PrimaryKeyField queryKey, Object value) throws Exception {

		
		List<Object> list = new ArrayList<>();
		File file = new File(CSVMetamodel.of(clss).getCSVFilePath(path));
		if(!file.exists())
		{
			return null;
		}
		
		FileReader fileReader =null;
		BufferedReader bufferedReader=null;
		fileReader =new FileReader(file);
		bufferedReader = new BufferedReader(fileReader);
		
		String columnNames = bufferedReader.readLine();
		Map<String,Integer> nameMap=getNameMap(columnNames);
		System.out.println("from class csvEntityManager");
		String line =null;
		while ((line=bufferedReader.readLine())!=null)   
		{ 
			String[] row = line.split(separator+"");
			
			
			if(row[nameMap.get(queryKey.getName())].equals(value.toString()))
			{
				return buildInstance(clss, row, CSVMetamodel.of(clss));
			}
			

		}
		return null;
	
    	
    	
    	
    } // find

	@Override
	public boolean inspectUnique(T t) throws Exception {
		// TODO Auto-generated method stub
		
		CSVMetamodel metamodel=CSVMetamodel.of(t.getClass());
		Class<T> clss = (Class<T>) t.getClass();
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
			Object[] obj=new Object[i];
			for(int itr=0;itr<obj.length;itr++)
			{
				Field field = uniqueFields[itr].getField();
				field.setAccessible(true);
				if(Modifier.isStatic(field.getModifiers()))
				{
					obj[itr]=field.get(null);
				}else
				{
					obj[itr] = field.get(t);
				}
			}
			boolean ifExists =  ifExists(clss, uniqueFields, obj, t);
			
			return ifExists;
			//sql=metamodel.buildSelectRequest(uniqueFields);
			//System.out.println("query to check unique::::"+sql);
			//ColumnField[] uniqueFields=uniqueFieldList;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			
		}
				//idGenerator = new AtomicLong();
		//String sql=metamodel.buildSelectRequest(un);
		
			
return false;
		}
	
	@Override
	public List findGenericMultipleConditions(Class<?> clss, ColumnField[] queryKey, Object[] value) throws Exception{

		// TODO Auto-generated method stub
		List list = new ArrayList<>();
		CSVMetamodel metamodel = CSVMetamodel.of(clss);
		PrimaryKeyField primaryField =metamodel.getPrimaryKey();
		File file = new File(metamodel.getCSVFilePath(path));
		if(!file.exists())
			return list;
		FileReader reader= null;
		BufferedReader bufferedReader = null;
		try {

		reader = new FileReader(file);
		bufferedReader=new BufferedReader(reader);
		List<String> array =new ArrayList<>();
		String line=null;
		String columnNames = bufferedReader.readLine();
		
		while((line = bufferedReader.readLine())!=null)
		{
			array.add(line);
		}
		

		
		Map<String,Integer> nameMap=new HashMap();
		
		int index=0;
		for(String columName:columnNames.split(separator+""))
		{
			nameMap.put(columName, index);
			index++;
		}
		for(String valueRow: array)
		{
			String[] valueRowArray = valueRow.split(separator+"");
			List paramList= IntStream.range(0, queryKey.length)
			.filter((i)->valueRowArray[nameMap.get(queryKey[i].getName())].equals(value[i].toString()))
			.mapToObj(i->value[i])
			.collect(Collectors.toList());
			if(paramList.size()==value.length)
			{list.add(buildInstance(clss, valueRowArray, nameMap));}
		}
		
		
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}finally
		{
			try {
				reader.close();
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return list;
	
	}
	
	private boolean ifExists(Class<T> clss, ColumnField[] queryKey, Object[] value, T t) {
		// TODO Auto-generated method stub
		CSVMetamodel metamodel = CSVMetamodel.of(t.getClass());
		PrimaryKeyField primaryField =metamodel.getPrimaryKey();
		File file = new File(metamodel.getCSVFilePath(path));
		FileReader reader= null;
		BufferedReader bufferedReader = null;
		try {
		if(!file.exists())
		{
			createFile(clss);
		}
		reader = new FileReader(file);
		bufferedReader=new BufferedReader(reader);
		List<String> array =new ArrayList<>();
		String line=null;
		String columnNames = bufferedReader.readLine();
		
		while((line = bufferedReader.readLine())!=null)
		{
			array.add(line);
		}
		
		if(array.size()==0)
			return false;
		
		Map<String, Integer> nameMap = getNameMap(columnNames);
		for(String valueRow: array)
		{
			String[] valueRowArray = valueRow.split(separator+"");
			List paramList= IntStream.range(0, queryKey.length)
			.filter((i)->valueRowArray[nameMap.get(queryKey[i].getName())].equals(value[i].toString()))
			.mapToObj(i->value[i])
			.collect(Collectors.toList());
			if(paramList.size()==value.length)
			{
				T cloneObject =buildInstance(clss, valueRowArray, metamodel);
				Field primaryKeyField = metamodel.getPrimaryKey().getField();
				primaryKeyField.setAccessible(true);
				primaryKeyField.set(t, primaryKeyField.get(cloneObject));
				return true;
			}
		}
		
		
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
		}finally
		{
			try {
				reader.close();
				bufferedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return false;
	}
	
	
	private T buildInstance(Class<T> clss, String[] resultSet,CSVMetamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		T t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		if(primaryKeyType==long.class)
		{
			long primaryKey=Long.valueOf(resultSet[nameMap.get(primaryKeyFieldName)]);
			primaryKeyField.setAccessible(true);
			primaryKeyField.set(t, primaryKey);
			
		}
		System.out.println(metamodel.getColumns());
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(columnType==int.class)
				{
					int value=Integer.valueOf(resultSet[nameMap.get(columnName)]);
					field.set(t, value);
				}else if(columnType==String.class)
				{
					String value=String.valueOf(resultSet[nameMap.get(columnName)]);
					field.set(t, value);
				}else if(columnType==long.class)
				{
					Long value=Long.valueOf(resultSet[nameMap.get(columnName)]);
					field.set(t, value);
				}
				else if(columnType.getSuperclass()== Object.class)
				{
					String filePath = path+resultSet[nameMap.get(columnName)]+".ser";
					Object value=readObjectFromFile(filePath);
					field.set(t, value);
				}
			}
			
			updateInjectListFields(metamodel, t);
		
		return t;
	}
	
	
	private Object buildInstance(Class<?> clss, String[] resultSet,Map<String,Integer> nameMap) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		CSVMetamodel metamodel = CSVMetamodel.of(clss);
		Object t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		if(primaryKeyType==long.class)
		{
			long primaryKey=Long.valueOf(resultSet[nameMap.get(primaryKeyFieldName)]);
			primaryKeyField.setAccessible(true);
			primaryKeyField.set(t, primaryKey);
			
		}
		System.out.println(metamodel.getColumns());
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(columnType==int.class)
				{
					int value=Integer.valueOf(resultSet[nameMap.get(columnName)]);
					field.set(t, value);
				}else if(columnType==String.class)
				{
					String value=String.valueOf(resultSet[nameMap.get(columnName)]);
					field.set(t, value);
				}else if(columnType==long.class)
				{
					Long value=Long.valueOf(resultSet[nameMap.get(columnName)]);
					field.set(t, value);
				}
				else if(columnType.getSuperclass()== Object.class)
				{
					String filePath = path+resultSet[nameMap.get(columnName)]+".ser";
					Object value=readObjectFromFile(filePath);
					field.set(t, value);
				}
			}
			updateInjectListFields(metamodel, t);
		return t;
	}

	private void updateUniqueID(T t) {
		CSVMetamodel metamodel = CSVMetamodel.of(t.getClass());
		FileReader reader= null;
		BufferedReader bufferedReader = null;
		try {
			File file = new File(metamodel.getCSVFilePath(path));
			reader = new FileReader(file);
			bufferedReader=new BufferedReader(reader);
			List<String> array =new ArrayList<>();
			String line=null;
			String columnNames = bufferedReader.readLine();
			while((line = bufferedReader.readLine())!=null)
			{
				array.add(line);
			}
			PrimaryKeyField primaryKeyField = metamodel.getPrimaryKey();
			Field field=primaryKeyField.getField();
			String primaryKeyFieldName = primaryKeyField.getName();
			field.setAccessible(true);		
			if(!file.exists() || array.size()==0)
			{
				int newID = RandomIntegerFactory.getRandomInteger();

				field.set(t,newID);
				return;
			}
			Map<String, Integer> nameMap = getNameMap(columnNames);
			HashSet<String> uniqueIDs = new HashSet<String>();
			for(String row: array)
			{
				String[] values = row.split(separator+"");
				System.out.println("&*&*&*::::"+primaryKeyFieldName+", nameMap::::"+nameMap+t.getClass()+"::::"+metamodel.getClassName());
				uniqueIDs.add(values[nameMap.get(primaryKeyFieldName)]);
				
			}
			int newID = RandomIntegerFactory.getRandomInteger();
			while(uniqueIDs.contains(newID))
			{
				newID = RandomIntegerFactory.getRandomInteger();
			}
			field.set(t, newID);
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				bufferedReader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	Map<String,Integer> nameMap=null;
	private Map<String, Integer> getNameMap(String columnNames) {
		if(nameMap!=null)
			return nameMap;
		 nameMap = new HashMap<String, Integer>();
		
		String[] columnNamesArray = columnNames.split(separator+"");
		
		for(int i=0;i<columnNamesArray.length;i++) {
			nameMap.put(columnNamesArray[i], i);
		}
		return nameMap;
	}

	private void createFile(Class<T> clss) {
		// TODO Auto-generated method stub
		CSVMetamodel metamodel = CSVMetamodel.of(clss);
		String joinedColumnNames = metamodel.getCommaSeparatedFieldNames(separator+"");
		
		File inputFile = new File(metamodel.getCSVFilePath(path));
		FileWriter fileWriter=null;
		try {
			fileWriter = new FileWriter(inputFile);
			fileWriter.write(joinedColumnNames);
			fileWriter.flush();
		} 		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private <T>EntityManager<T> wrap(Stack<List<Wrapper>> stack,String path, Map<Class<?>,List<Wrapper>> wrapperListToClassMap)
	{
		return new CSVEntityManager<T>(stack,path, wrapperListToClassMap);
	}
	
	private CSVEntityManager(Stack<List<Wrapper>> stack,String path, Map<Class<?>,List<Wrapper>> wrapperListToClassMap) {
		super();
		this.stack = stack;
		this.path=path;
		this.wrapperListToClassMap=wrapperListToClassMap;
	}
	
	@Override
	protected <U>EntityManager<U> classToObjGenericWrap(Class<U> clss)
	{
		
		return wrap(stack,path, wrapperListToClassMap);
	}
	
	@Override
	public List<T> find(Query<T> query) {
		if(query.getJoins()!=null && !query.getJoins().isEmpty()) {
			throw new RuntimeException("For Join this method incompatible Please call method findWithJoin");
		}
		Executor executor = new InMemoryExecutor();	
		List<T> list = null;
		try {
			list =(List<T>)buildInstance(clss, CSVMetamodel.of(clss));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return executor.execute(query, list);
	}
	
	@Override
	public List<JoinedRow> findWithJoin(Query<T> query) {
		// TODO Auto-generated method stub
		Executor executor = new InMemoryExecutor();	
		Map<Class<?>, List<?>> entityDataListMap = new HashMap<>();
		for(Join<?, ?> join: query.getJoins()) {
			try {
			if(entityDataListMap.get(join.getLeftEntity()) == null)
				entityDataListMap.put(join.getLeftEntity(), buildInstance(join.getLeftEntity(), CSVMetamodel.of(join.getLeftEntity())));
			if(entityDataListMap.get(join.getRightEntity()) == null)
				entityDataListMap.put(join.getRightEntity(), buildInstance(join.getRightEntity(), CSVMetamodel.of(join.getRightEntity())));
			}catch(Exception e) {
				System.out.println("Error in build instance: "+e.getMessage());
			}
		}
		return executor.executeWithJoin(query, entityDataListMap);
	}

}
