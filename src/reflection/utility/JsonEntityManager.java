package reflection.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
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
import query.tree.SqlExecutor;
import util.ColumnField;
import util.JsonMetamodel;
import util.Metamodel;
import util.PrimaryKeyField;
import util.XMLMetamodel;

public class JsonEntityManager<T> extends RelationJoinManager<T> {
	@Inject(classname="")
	String path;
	
	@uniqueIdUtil(uniqueIdUtilClass = RandomIntegerFactory.class)
	IntegerFactory integerFactory;
	
	
	
	public JsonEntityManager() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void persistFinalObject(T t) {
		JsonMetamodel metamodel = JsonMetamodel.of(t.getClass());
		File inputFile = new File(metamodel.getJsonFilePath(path));
		FileWriter fileWriter =null;
		FileReader fileReader =null;
		try {
		if(!inputFile.exists())
		{
			createFile((Class<T>)t.getClass());
			inputFile = new File(metamodel.getJsonFilePath(path));
			

		}
		fileReader =new FileReader(inputFile);
		JSONArray jsonArray = (JSONArray)(new JSONParser()).parse(fileReader);
		updateUniqueID(t, jsonArray);
		JSONObject obj = getJsonObjectFromInstance_temp(metamodel,t);
		jsonArray.add(obj);
		fileWriter = new FileWriter(inputFile);
		fileWriter.write(jsonArray.toJSONString());
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	@Override
	public void update(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		JsonMetamodel metamodel = JsonMetamodel.of(t.getClass());
		File inputFile = new File(metamodel.getJsonFilePath(path));
		FileWriter fileWriter =null;
		FileReader fileReader =null;
		try {
		if(!inputFile.exists())
		{persist(t);
			return;

		}
		fileReader = new FileReader(inputFile);
		JSONArray array = (JSONArray)(new JSONParser()).parse(fileReader);
		PrimaryKeyField primaryKeyField = metamodel.getPrimaryKey();
		Field field=primaryKeyField.getField();
		field.setAccessible(true);	
		Object val = field.get(t);
		//System.out.printlnval);
		for(Object o:array)
		{
			JSONObject obj = (JSONObject)o;
			if(obj.get(primaryKeyField.getName()).toString().equals(val.toString()))
			{
				System.out.println("updated!!!!");
				updateObjectInJson(obj,t,metamodel);
				break;
			}
			
		}
		fileWriter = new FileWriter(inputFile);
		fileWriter.write(array.toJSONString());
		fileWriter.flush();
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			try {
				fileReader.close();
				fileWriter.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public List<T> find(Class<T> class1, Predicate<T> predicate) throws Exception {
		// TODO Auto-generated method stub
	JsonMetamodel metamodel=JsonMetamodel.of(class1);
	List<T> list = new ArrayList();
	File file = new File(metamodel.getJsonFilePath(path));
	if(!file.exists())
	{
		return null;
	}
	FileReader reader=null;
	try {
		reader = new FileReader(file);
		JSONArray array = (JSONArray)(new JSONParser()).parse(reader);
		for(Object o:array)
		{
			JSONObject obj = (JSONObject)o;

					//System.out.println(queryKey[0]);

						T t =(T) buildInstance(class1, obj,metamodel);
						if(predicate.test(t))
							list.add(t);
					
			
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	finally {
		reader.close();
	}
	return list;
}
	
	private void updateObjectInJson_temp(JSONObject obj, T t, JsonMetamodel metamodel) {
		// TODO Auto-generated method stub
		searchAndUpdateDependency(t);
		PrimaryKeyField primaryKey = metamodel.getPrimaryKey();
		Field primaryKeyField = primaryKey.getField();
		primaryKeyField.setAccessible(true);
		try {
			String primaryKeyValue = primaryKeyField.get(t).toString();
			//obj.put(primaryKey.getName(), primaryKeyField.get(t));
			List<ColumnField> columnFieldList = metamodel.getColumns();
			for(ColumnField cl:columnFieldList)
			{
				Field columnField = cl.getField();
				columnField.setAccessible(true);
				Class<?> cfType = columnField.getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					File dir_class= new File(path+t.getClass().getName());
					if(!dir_class.exists())
					{
					
					dir_class.mkdir();
					}
					
					saveToFile(t, primaryKeyValue, cl,path);
					obj.put(cl.getName(), t.getClass().getName()+File.separator+cl.getName()+"_"+primaryKeyValue);
				}
				else
				obj.put(cl.getName(), columnField.get(t));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void updateObjectInJson(JSONObject obj, T t, JsonMetamodel metamodel) {
		// TODO Auto-generated method stub
		searchAndUpdateDependency(t);
		PrimaryKeyField primaryKey = metamodel.getPrimaryKey();
		Field primaryKeyField = primaryKey.getField();
		primaryKeyField.setAccessible(true);
		try {
			String primaryKeyValue = primaryKeyField.get(t).toString();
			//obj.put(primaryKey.getName(), primaryKeyField.get(t));
			List<ColumnField> columnFieldList = metamodel.getColumns();
			for(ColumnField cl:columnFieldList)
			{
				Field columnField = cl.getField();
				columnField.setAccessible(true);
				Class<?> cfType = columnField.getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					Object instanceObj = columnField.get(t);
					JSONObject childObj = (JSONObject)obj.get(cl.getName());
					updateObjectInJsonGeneric(childObj, instanceObj);
					
				}
				else
				obj.put(cl.getName(), columnField.get(t));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void updateObjectInJsonGeneric(JSONObject obj, Object t) {
		// TODO Auto-generated method stub
		JsonMetamodel metamodel = JsonMetamodel.of(t.getClass());
		try {
			
			//obj.put(primaryKey.getName(), primaryKeyField.get(t));
			List<ColumnField> columnFieldList = metamodel.getColumns();
			for(ColumnField cl:columnFieldList)
			{
				Field columnField = cl.getField();
				columnField.setAccessible(true);
				Class<?> cfType = columnField.getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					Object instanceObj = columnField.get(t);
					JSONObject childObj = (JSONObject)obj.get(cl.getName());
					updateObjectInJsonGeneric(childObj, instanceObj);
				}
				else
				obj.put(cl.getName(), columnField.get(t));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	


	private void updateUniqueID(T t, JSONArray array) {
	JsonMetamodel metamodel = JsonMetamodel.of(t.getClass());
		try {
			
			//JSONArray array =(JSONArray)(new JSONParser()).parse(reader);
			PrimaryKeyField primaryKeyField = metamodel.getPrimaryKey();
			Field field=primaryKeyField.getField();
			field.setAccessible(true);		
			if(array.size()==0)
			{
				int newID = integerFactory.returnRandomInteger();
	
				field.set(t,newID);
				return;
			}
			HashSet<String> uniqueIDs = new HashSet<String>();
			for(Object o: array)
			{
				JSONObject obj=(JSONObject) o;
				uniqueIDs.add(obj.get(primaryKeyField.getName()).toString());
				
			}
			int newID = integerFactory.returnRandomInteger();
			while(uniqueIDs.contains(newID))
			{
				newID = integerFactory.returnRandomInteger();
			}
			field.set(t, newID);
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
	}
	
	private JSONObject getJsonObjectFromInstance(JsonMetamodel metamodel, T t) {
		// TODO Auto-generated method stub
		searchAndUpdateDependency(t);
		JSONObject obj = metamodel.getNewJsonObject();
		PrimaryKeyField primaryKey = metamodel.getPrimaryKey();
		Field primaryKeyField = primaryKey.getField();
		primaryKeyField.setAccessible(true);
		try {
			String primaryKeyValue = primaryKeyField.get(t).toString();
			obj.put(primaryKey.getName(), primaryKeyField.get(t));
			List<ColumnField> columnFieldList = metamodel.getColumns();
			for(ColumnField cl:columnFieldList)
			{
				Field columnField = cl.getField();
				columnField.setAccessible(true);
				Class<?> cfType = columnField.getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					File dir_class= new File(path+t.getClass().getName());
					if(!dir_class.exists())
					{
					
					dir_class.mkdir();
					}
					
					saveToFile(t, primaryKeyValue, cl,path);
					obj.put(cl.getName(), t.getClass().getName()+File.separator+cl.getName()+"_"+primaryKeyValue);
				}
				else
				obj.put(cl.getName(), columnField.get(t));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
	
	
	private JSONObject getJsonObjectFromInstance_temp(JsonMetamodel metamodel, T t) {
		// TODO Auto-generated method stub
		searchAndUpdateDependency(t);
		JSONObject obj = metamodel.getNewJsonObject();
		PrimaryKeyField primaryKey = metamodel.getPrimaryKey();
		Field primaryKeyField = primaryKey.getField();
		primaryKeyField.setAccessible(true);
		try {
			String primaryKeyValue = primaryKeyField.get(t).toString();
			obj.put(primaryKey.getName(), primaryKeyField.get(t));
			List<ColumnField> columnFieldList = metamodel.getColumns();
			for(ColumnField cl:columnFieldList)
			{
				Field columnField = cl.getField();
				columnField.setAccessible(true);
				Class<?> cfType = columnField.getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					JsonMetamodel colFieldMetamodel = JsonMetamodel.of(cfType);
					JSONObject colFieldObject = colFieldMetamodel.getNewJsonObjectWithoutPrimaryKey();
					colFieldObject = updateAndreturnColFieldObject(colFieldObject, colFieldMetamodel, columnField.get(t));
					obj.put(cl.getName(), colFieldObject);
					
				}
				else
				obj.put(cl.getName(), columnField.get(t));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return obj;
	}
	
	
	
	
	
	
	protected <U>EntityManager<U> classToObjGenericWrapJson(Class<U> clss)
	{
		
		return new JsonEntityManager<>();
	}
	
	
	public void createFile(Class<T> clss) {
		// TODO Auto-generated method stub
		FileWriter fileWriter=null;
		try {
			JsonMetamodel metamodel = JsonMetamodel.of(clss);
			File file = new File(metamodel.getJsonFilePath(path));
			if(file.exists())
			{
				System.out.println("File already exists, Process will be aborted.");
				return;
			}
		fileWriter = new FileWriter(file);
		fileWriter.write("[]");
		fileWriter.flush();
		//fileWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				fileWriter.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
	}
	
	private JSONObject updateAndreturnColFieldObject(JSONObject colFieldObject, JsonMetamodel colFieldMetamodel, Object obj) {
		
		if(obj == null) {
			return colFieldObject;
		}
		
		List<ColumnField> columnFieldList = colFieldMetamodel.getColumns();
		for(ColumnField cl:columnFieldList)
		{
			Field columnField = cl.getField();
			columnField.setAccessible(true);
			Class<?> cfType = columnField.getType();
			if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
			{
				JsonMetamodel colFieldNestedMetamodel = JsonMetamodel.of(cfType);
				JSONObject colFieldNestedObject = colFieldMetamodel.getNewJsonObjectWithoutPrimaryKey();
				try {
					colFieldNestedObject = updateAndreturnColFieldObject(colFieldNestedObject, colFieldNestedMetamodel, columnField.get(obj));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				colFieldObject.put(cl.getName(), colFieldNestedObject);
				
			} else {
					try {
						colFieldObject.put(cl.getName(), columnField.get(obj));
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
		}
		
		return colFieldObject;
	}
	
	
	
	@Override
	public void createTable(Class<T> clss)
	{
		System.out.println(" This feature not supported for JsonEntities");
	}
	
	@Override
	public boolean inspectUnique(T t) throws Exception
	{
		// TODO Auto-generated method stub
		
		JsonMetamodel metamodel=JsonMetamodel.of(t.getClass());
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
	
	
	private boolean ifExists(Class<T> clss, ColumnField[] uniqueFields, Object[] obj, T t) {
			// TODO Auto-generated method stub
		List<Object> list = new ArrayList<>();
		JsonMetamodel metamodel = JsonMetamodel.of(clss);
		try {
			File file = new File(metamodel.getJsonFilePath(path));
			if(!file.exists())
			{
				return false;
			}
			FileReader reader = new FileReader(file);
			JSONParser jsonParser = new JSONParser();
			Object fileObj = jsonParser.parse(reader);
			JSONArray entityArray = (JSONArray) fileObj;
			
			for(Object o : entityArray)
			{
				JSONObject jsonObject = (JSONObject)o;
				
				List paramList= IntStream.range(0, uniqueFields.length)
						.filter((i)->jsonObject.get(uniqueFields[i].getName()).equals(obj[i].toString()))
						.mapToObj(i->obj[i])
						.collect(Collectors.toList());
				
				if(paramList.size()==obj.length)
				{
					T cloneObject =(T)buildInstance(clss, jsonObject, metamodel);
					Field primaryKeyField = metamodel.getPrimaryKey().getField();
					primaryKeyField.setAccessible(true);
					primaryKeyField.set(t, primaryKeyField.get(cloneObject));
					return true;
				}
			}
			
		reader.close();
		
		}
		 catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return false;
	
	}
	
	
	
	@Override
	public void updateBasedOnUnique(T t) throws SQLException, IllegalArgumentException, IllegalAccessException
	{
		try
		{

			update(t);

		}
		catch(Exception e)
		{
			System.out.println("Issue in update ::::"+e);
		}
		
	}
	
	private Object buildInstance(Class<?> clss, JSONObject resultSet, JsonMetamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		Object t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		if(primaryKeyType==long.class)
		{
			long primaryKey=(long) resultSet.get(primaryKeyFieldName);
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
				{//System.out.println("**************"+resultSet.get(columnName).getClass());
					int value=Integer.valueOf(resultSet.get(columnName).toString());
					field.set(t, value);
				}else if(columnType==String.class)
				{
					String value=String.valueOf(resultSet.get(columnName));
					field.set(t, value);
				}else if(columnType==long.class)
				{
					//System.out.println("**************"+resultSet.get(columnName).getClass());
					Long value=(Long) resultSet.get(columnName);
					field.set(t, value);
				}
				else if(columnType == double.class) {
					
					double value = Double.valueOf(resultSet.get(columnName).toString());
					field.set(t, value);
					
				}
				else if(columnType.getSuperclass()== Object.class)
				{
					
					JSONObject nestedObject =  (JSONObject) resultSet.get(columnName);
					if(nestedObject!=null) {
						JsonMetamodel nestedObjectMetamodel = JsonMetamodel.of(columnType);
						
						Object obj = getNestedObjectForObjectTypeColumn(columnType, nestedObject,
								nestedObjectMetamodel);
						field.set(t, obj);
					}

					
				}
			}
			updateInjectListFields(metamodel, t);
		return t;
	}


	private Object getNestedObjectForObjectTypeColumn(Class<?> columnType,
			JSONObject nestedObject, JsonMetamodel nestedObjectMetamodel)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Object obj = columnType.getConstructor().newInstance();
		
		for(ColumnField colField:nestedObjectMetamodel.getColumns()) {
			Field nestedField = colField.getField();
			String nestedObjectColumnName = colField.getName();
			nestedField.setAccessible(true);
			Class<?> nestedFieldColType = nestedField.getType();
			if(nestedFieldColType==int.class)
			{//System.out.println("**************"+resultSet.get(nestedObjectColumnName).getClass());
				int value=Integer.valueOf(nestedObject.get(nestedObjectColumnName).toString());
				nestedField.set(obj, value);
			}else if(nestedFieldColType==String.class)
			{
				String value=String.valueOf(nestedObject.get(nestedObjectColumnName));
				nestedField.set(obj, value);
			}else if(nestedFieldColType==long.class)
			{
				//System.out.println("**************"+resultSet.get(nestedObjectColumnName).getClass());
				Long value=(Long) nestedObject.get(nestedObjectColumnName);
				nestedField.set(obj, value);
			}
			else if(nestedFieldColType==double.class)
			{//System.out.println("**************"+resultSet.get(nestedObjectColumnName).getClass());
				double value=Double.valueOf(nestedObject.get(nestedObjectColumnName).toString());
				nestedField.set(obj, value);
			}
			
			else if(columnType.getSuperclass()== Object.class)
			{
				
				JSONObject nestedObjectObject =  (JSONObject) nestedObject.get(nestedObjectColumnName);
				if(nestedObjectObject!=null) {
					
					JsonMetamodel nestedObjectObjectMetamodel = JsonMetamodel.of(nestedFieldColType);
					
					Object resultObject = getNestedObjectForObjectTypeColumn(columnType, nestedObjectObject,
							nestedObjectObjectMetamodel);
					nestedField.set(obj, resultObject);
					
				}

				
			}
		}
		return obj;
	}
	
	private Object buildInstance_temp(Class<?> clss, JSONObject resultSet, JsonMetamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		Object t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		if(primaryKeyType==long.class)
		{
			long primaryKey=(long) resultSet.get(primaryKeyFieldName);
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
				{//System.out.println("**************"+resultSet.get(columnName).getClass());
					int value=Integer.valueOf(resultSet.get(columnName).toString());
					field.set(t, value);
				}else if(columnType==String.class)
				{
					String value=String.valueOf(resultSet.get(columnName));
					field.set(t, value);
				}else if(columnType==long.class)
				{
					//System.out.println("**************"+resultSet.get(columnName).getClass());
					Long value=(Long) resultSet.get(columnName);
					field.set(t, value);
				}else if(columnType.getSuperclass()== Object.class)
				{
					if(resultSet.get(columnName)!=null)
					{
					String filePath = path+(resultSet.get(columnName))+".ser";
					Object value=readObjectFromFile(filePath);
					field.set(t, value);
					}
				}
			}
			updateInjectListFields(metamodel, t);
		return t;
	}
	
	
	
	
	
	@Override
	public T find(Class<T> class1, Object primaryKey) throws Exception {
		// TODO Auto-generated method stub
		JsonMetamodel metamodel=JsonMetamodel.of(class1);
		
		
		File file = new File(metamodel.getJsonFilePath(path));
		if(!file.exists())
		{
			return null;
		}
		FileReader reader = new FileReader(file);
		JSONArray array = (JSONArray)(new JSONParser()).parse(reader);
		PrimaryKeyField primaryKeyField = metamodel.getPrimaryKey();
		String fieldName = primaryKeyField.getName();
		for(Object o:array)
		{
			JSONObject obj = (JSONObject)o;
			if(obj.get(fieldName).equals(primaryKey))
			{
				Object t= buildInstance(class1, obj, metamodel);
				return (T)t;
			}
			
		}
		
		return null;
		
	}
	
    @Override
	public List findGeneric(Class<?> clss, ColumnField queryKey, Object value) throws Exception {
    	return findGenericMultipleConditions(clss, new ColumnField[] {queryKey}, new Object[] {value});
    	}
    
	@Override
	public List find(Class<T> class1, String columnName, Object columnValue) throws Exception {
		// TODO Auto-generated method stub
		JsonMetamodel metamodel=JsonMetamodel.of(class1);
		List list = new ArrayList();
		File file = new File(metamodel.getJsonFilePath(path));
		FileReader reader=null;
		try {
			reader = new FileReader(file);
			JSONArray array = (JSONArray)(new JSONParser()).parse(reader);
			for(Object o:array)
			{
				JSONObject obj = (JSONObject)o;
				if(obj.get(columnName).equals(columnValue))
				{
					Object t= buildInstance(class1, obj, metamodel);
					list.add((T)t);
					
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			reader.close();
		}
		return list;
	}
	
	
	@Override
	public List find(Class<?> class1, Object columnValue,  String columnName) throws Exception {
		// TODO Auto-generated method stub
		JsonMetamodel metamodel=JsonMetamodel.of(class1);
		List list = new ArrayList();
		File file = new File(metamodel.getJsonFilePath(path));
		FileReader reader=null;
		try {
			reader = new FileReader(file);
			JSONArray array = (JSONArray)(new JSONParser()).parse(reader);
			for(Object o:array)
			{
				JSONObject obj = (JSONObject)o;
				if(obj.get(columnName).equals(columnValue))
				{
					Object t= buildInstance(class1, obj, metamodel);
					list.add((T)t);
					
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			reader.close();
		}
		return list;
	}
	
	@Override
	public List findGenericMultipleConditions(Class<?> clss, ColumnField[] queryKey, Object[] value) throws Exception{
			// TODO Auto-generated method stub
		JsonMetamodel metamodel=JsonMetamodel.of(clss);
		List list = new ArrayList();
		File file = new File(metamodel.getJsonFilePath(path));
		FileReader reader=null;
		try {
			reader = new FileReader(file);
			JSONArray array = (JSONArray)(new JSONParser()).parse(reader);
			for(Object o:array)
			{
				JSONObject obj = (JSONObject)o;
				List paramList= IntStream.range(0, queryKey.length)
						.filter((i)->obj.get(queryKey[i].getName()).toString().equals(value[i].toString()))
						.mapToObj(i->value[i])
						.collect(Collectors.toList());
						//System.out.println(queryKey[0]);
						if(paramList.size()==value.length)
						{
							list.add(buildInstance(clss, obj,metamodel));
						}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			reader.close();
		}
		return list;
	}
	
	
	 @Override
		protected List getEntityList(Class<?>[] clss, int index) throws SQLException, Exception {
			JsonMetamodel metamodelT = JsonMetamodel.of(clss[index]);		
			List allTElements = null;
			allTElements=buildInstance(clss[0], metamodelT);
			return allTElements;
		}

	private List buildInstance(Class<?> clss, JsonMetamodel metamodelT) {
		// TODO Auto-generated method stub
		
		List list = new ArrayList();
		File file = new File(metamodelT.getJsonFilePath(path));
		FileReader reader=null;
		try {
			reader = new FileReader(file);
			JSONArray array = (JSONArray)(new JSONParser()).parse(reader);
			for(Object o:array)
			{
				JSONObject obj = (JSONObject)o;

					Object t= buildInstance(clss, obj, metamodelT);
					
					updateInjectListFields(metamodelT, t);
					list.add(t);
					
				
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	
	 @Override
	 protected List buildInstance(Class clss) throws SQLException, Exception {
			JsonMetamodel metamodelT = JsonMetamodel.of(clss);		
			List allTElements = null;
			allTElements=buildInstance(clss, metamodelT);
			return allTElements;
		}
	 
		@Override
		public List<T> buildInstances(Class<T> clss) {
			// TODO Auto-generated method stub
			System.out.println("this one called");
			List<T> data = null;
			
			try {
				data = buildInstance(clss);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return data;
		}
	 
	
//		private <U>EntityManager<U> wrap(Stack<List<Wrapper>> stack,String path, Map<Class<?>,List<Wrapper>> wrapperListToClassMap)
//		{
//			return new JsonEntityManager<U>(stack,path, wrapperListToClassMap);
//		}
		
		private JsonEntityManager(Stack<List<Wrapper>> stack,String path, Map<Class<?>,List<Wrapper>> wrapperListToClassMap) {
			super();
			this.stack = stack;
			this.path=path;
			this.wrapperListToClassMap=wrapperListToClassMap;
		}
		
		
		@Override
		protected <U>EntityManager<U> classToObjGenericWrap(Class<U> clss)
		{
			
			return new JsonEntityManager<U>(stack,path, wrapperListToClassMap);
		}
		
		@Override
		public List<T> find(Query<T> query) {
			if(query.getJoins()!=null && !query.getJoins().isEmpty()) {
				throw new RuntimeException("For Join this method incompatible Please call method findWithJoin");
			}
			Executor executor = new InMemoryExecutor();	
			return executor.execute(query, buildInstance(clss, JsonMetamodel.of(clss)));
		}
		
		@Override
		public List<JoinedRow> findWithJoin(Query<T> query) {
			// TODO Auto-generated method stub
			Executor executor = new InMemoryExecutor();	
			Map<Class<?>, List<?>> entityDataListMap = new HashMap<>();
			for(Join<?, ?> join: query.getJoins()) {
				if(entityDataListMap.get(join.getLeftEntity()) == null)
					entityDataListMap.put(join.getLeftEntity(), buildInstance(join.getLeftEntity(), JsonMetamodel.of(join.getLeftEntity())));
				if(entityDataListMap.get(join.getRightEntity()) == null)
					entityDataListMap.put(join.getRightEntity(), buildInstance(join.getRightEntity(), JsonMetamodel.of(join.getRightEntity())));
			}
			return executor.executeWithJoin(query, entityDataListMap);
		}

}
