package reflection.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.LinkOption;
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import objectPoolPattern.pool.ObjectPool;
import query.tree.Executor;
import query.tree.InMemoryExecutor;
import query.tree.Query;
import reflection.utility.ManagedEntityManager.PreparedStatementWrapper;
import util.ColumnField;
import util.GroupMetricsManager;
import util.JsonMetamodel;
import util.Metamodel;
import util.MetricsManager;
import util.PrimaryKeyField;

public class RelationJoinManager<T> extends ManagedEntityManager<T> {
	
	private class PreparedStatementWrapperChild extends ManagedEntityManager<T>.PreparedStatementWrapper
	{
		PreparedStatement psChild;

		public PreparedStatement getStatement() {
			return psChild;
		}



		public void setStatement(PreparedStatement psChild) {
			this.psChild = psChild;
		}



		public PreparedStatementWrapperChild(PreparedStatement preparedStatement) {
			super(preparedStatement);
			this.psChild=preparedStatement;
			// TODO Auto-generated constructor stub
		}
		

		
		public PreparedStatement andQueryKey(Object queryKey) throws SQLException {
			System.out.println("This method called");
			// TODO Auto-generated method stub
			if(queryKey.getClass()==Long.class)
			{
				psChild.setLong(1, (Long)queryKey);
			}
			else if(queryKey.getClass()==int.class)
			{
				psChild.setInt(1, (int)queryKey);
			}
			else if(queryKey.getClass()==String.class)
			{
				psChild.setString(1, (String)queryKey);
			}
			return psChild;
		}
		
		public PreparedStatement andQueryKeyWithMultipleConditions(Object[] queryKey) throws SQLException {
			System.out.println("This method called");
			// TODO Auto-generated method stub
			for(int i=0;i<queryKey.length;i++)
			{
				Object key=queryKey[i];
				System.out.println("queryKey-->"+i+"<-->"+key);
				
			if(key.getClass()==Long.class)
			{
				psChild.setLong(i+1, (Long)key);
			}
			else if(key.getClass()==int.class)
			{
				psChild.setInt(i+1, (int)key);
			}
			else if(key.getClass()==String.class)
			{
				psChild.setString(i+1, (String)key);
			}
			}
			return psChild;
		}
		
	}
	//@Inject
	//Connection connection;
	public RelationJoinManager() {
		super();
		//connection =null;
		// TODO Auto-generated constructor stub
	}
	
	private List buildInstance(Class<?> clss, ResultSet resultSet, Metamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		List list = new ArrayList();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		while(resultSet.next())
		{
			Object u = clss.getConstructor().newInstance();
		if(primaryKeyType==long.class)
		{
			long primaryKey=resultSet.getInt(primaryKeyFieldName);
			primaryKeyField.setAccessible(true);
			primaryKeyField.set(u, primaryKey);
			
		}
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(columnType==int.class)
				{
					int value=resultSet.getInt(columnName);
					field.set(u, value);
				}else if(columnType==String.class)
				{
					String value=resultSet.getString(columnName);
					field.set(u, value);
				}else if(columnType==long.class)
				{
					
					Long value=resultSet.getLong(columnName);
					field.set(u, value);
					
				}else if(columnType.getSuperclass()==Object.class)
				{
					byte[] value = resultSet.getBytes(columnName);
				    if(value!=null)
				    {
						ByteArrayInputStream in = new ByteArrayInputStream(value);
					    ObjectInputStream is = new ObjectInputStream(in);
					    field.set(u, is.readObject());
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
			
			List<Field> injectFields = metamodel.getInjectList();
			injectFields.stream().forEach((field)->{
		
				try {
				
				Class<?> injectclss = getGenericClassRuntime(field);
				field.setAccessible(true);
				// to inject dependenct objects value
				field.set(u, join(u,injectclss));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			});
			
		list.add(u);
		}
		
		return list;
	}
	

	
	private Map<Class<?>, Supplier<?>> registry=new HashMap();
	private Map<String, Supplier<?>> registrySearch=new HashMap();
	
	
	
	public List<T> pivotColumn(Class<T> clss, String pivotColumnName) throws Exception
	{
		List<T> list =(List) buildInstance(clss);
		Metamodel metamodel = Metamodel.of(clss);
		ColumnField pivotColumnField = metamodel.getNameFieldMapping().get(pivotColumnName);
		if(pivotColumnField == null || pivotColumnField.getField().getType()!=String.class || pivotColumnField.getField().getAnnotation(pivot.class)==null)
		{
			System.out.println("pivot column should be of string type or annotation pivot is missing.");
			return null;
		}
		
		if(pivotColumnField.getField().getAnnotation(unique.class)!=null)
		{
			System.out.println("pivot column cant be unique.");
			return null;
		}
		
	Field pivotField = pivotColumnField.getField();
	pivotField.setAccessible(true);
		
		Map<String,T> instance_map = new HashMap<>();
		for(T obj: list)
		{
			String key = (String)pivotField.get(obj);
			key = key==null?"":key;
			if(instance_map.get(key)==null )
			{
				instance_map.put(key, obj);
			}
			else
			{
				
				T pivotObject = instance_map.get(key);
				List<ColumnField> objColumns = metamodel.getColumns();
				for(ColumnField cl:objColumns)
				{
					Field field = cl.getField();
					field.setAccessible(true);
					
					if(field.getType()==long.class)
					{
						field.set(pivotObject, (Long)field.get(obj)+(Long)field.get(pivotObject));
					}else if(field.getType()==int.class)
					{
						field.set(pivotObject, (int)field.get(obj)+(int)field.get(pivotObject));
					}else if(field.getType()==double.class)
					{
						field.set(pivotObject, (double)field.get(obj)+(double)field.get(pivotObject));
					}
				}
			}
			
			
		}
		
		return (List<T>)instance_map.entrySet().stream().map((entry)->entry.getValue()).collect(Collectors.toList());
		
		
		
		
		
		//return list;
	}
	
	@Override
	public List<T> pivotColumn(Class<T> clss, String[] pivotColumnName) throws Exception
	{
		List<T> list =(List<T>) buildInstance(clss);
		Metamodel metamodel = Metamodel.of(clss);
		Predicate<ColumnField> checkPivot = (p)-> p == null || p.getField().getType()!=String.class || p.getField().getAnnotation(pivot.class)==null;
		//ColumnField pivotColumnField = metamodel.getNameFieldMapping().get(pivotColumnName);
		List<ColumnField> pivotColumnFields = Arrays.stream(pivotColumnName)
				.map((pcn)->metamodel.getNameFieldMapping().get(pcn))
				.collect(Collectors.toList());
		
		List<Boolean> pivotCheck = pivotColumnFields.stream()
													.map((pcf)->checkPivot.test(pcf))
													.collect(Collectors.toList());
		System.out.println(pivotCheck);
		if(pivotCheck.contains(true))
		{
			System.out.println("one of the pivot column provided didn't match the criteria--> should be of string type or annotation pivot is missing.");
			return null;
		}
		

		
	List<Field> pivotFields = pivotColumnFields.stream()
												.map((pcf)->pcf.getField())
												.collect(Collectors.toList());
	
	List<String> pivotFieldsNames = pivotFields.stream()
												.map((pf)->pf.getName())
												.collect(Collectors.toList());
	pivotFields.stream().forEach((pf)->pf.setAccessible(true));
		
		Map<String,T> instance_map = new HashMap<>();
		for(T obj: list)
		{
			String key = String.join("$",pivotFields.stream()
					.map((pf)->{
						try {
							return pf.get(obj)==null?"":(String)pf.get(obj);	
						}
						catch(Exception e)
						{
						return "";}})
					.collect(Collectors.toList()));
			if(instance_map.get(key)==null )
			{
				instance_map.put(key, obj);
			}
			else
			{
				
				T pivotObject = instance_map.get(key);
				List<ColumnField> objColumns = metamodel.getColumns();
				for(ColumnField cl:objColumns)
				{
					Field field = cl.getField();
					field.setAccessible(true);
					
					if(field.getType()==long.class)
					{
						field.set(pivotObject, (Long)field.get(obj)+(Long)field.get(pivotObject));
					}else if(field.getType()==int.class)
					{
						field.set(pivotObject, (int)field.get(obj)+(int)field.get(pivotObject));
					}else if(field.getType()==double.class)
					{
						field.set(pivotObject, (double)field.get(obj)+(double)field.get(pivotObject));
					}
					else if(field.getType()==String.class && !pivotFieldsNames.contains(field.getName()))
					{
						field.set(pivotObject, (field.get(obj)== null?"":field.get(obj)).toString()+","+(field.get(pivotObject)==null?"":field.get(pivotObject)).toString());
					}
				}
			}
			
			
		}
		
		System.out.println("<><><><><>"+instance_map);
		
		return (List<T>)instance_map
				.entrySet().stream()
				.map((entry)->entry.getValue())
				.collect(Collectors.toList());
		
		
		
		
		
		//return list;
	}
	
	
	

//	@Override
//	public Map genericJoin(Class<?> classT, Class<?> classU) {
//		
//		Map<Object,List> genericJoinMap = new HashMap();
//		Metamodel metamodelT = Metamodel.of(classT);
//		String sql=metamodelT.buildSelectRequestAllRows();
//		List allTElements = null;
//		try(PreparedStatement statement=prepareStatementWith(sql).getPreparedStatement();
//		ResultSet resultSet=statement.executeQuery();)
//		{
//		allTElements=buildInstance(classT, resultSet,metamodelT);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ArrayList allUElementsConneted = null;
//		for(Object t:allTElements)
//		{
//			allUElementsConneted=(ArrayList) join(t,classU);
//			genericJoinMap.put(t, allUElementsConneted);
//			
//		}
//		return genericJoinMap;
//		// TODO Auto-generated method stub
//		
//	}
	
	@Override
	public Node genericJoin(Class<?>... classes) {
		// TODO Auto-generated method stub

		Node root=new Node();
		root.setData("root node");
generateList(root, classes, 0);
		
		
		
		return root;
		// TODO Auto-generated method stub
		
	
		
	}
	
	public void generateList(Node node, Class<?>[] clss, int index)
	{
		System.out.println("length for class:::"+clss.length);
		node.setLevel(index);
		if(index>=clss.length)
			return;
		// it's only requires to find object at first level
		if(index==0)
		{
			try
			{
				List allTElements = getEntityList(clss, index);
			
			for(Object o:allTElements)
			{
				System.out.println("for level 0-->"+o);
				Node successorNode=new Node(o);
				node.addToList(successorNode);
				
				generateList(successorNode, clss, index+1);
				
			}

			
			
			
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else
		{
			
			List elements = (ArrayList) join(node.getData(),clss[index]);
			for(Object o:elements)
			{
				System.out.println("for level 1 and above-->"+o);
				Node successorNode=new Node(o);
				node.addToList(successorNode);
				generateList(successorNode, clss, index+1);
				
			}

		}
	}
	


	protected List buildInstance(Class clss) throws SQLException, Exception {
		Metamodel metamodelT = Metamodel.of(clss);
		String sql=metamodelT.buildSelectRequestAllRows();
		List allTElements = null;
		PreparedStatement statement=prepareStatementWith(sql).getPreparedStatement();
		ResultSet resultSet=statement.executeQuery();
		allTElements=buildInstance(clss, resultSet,metamodelT);
		
		return allTElements;
	}
	
	
	// works for only comparable type of objects that is. classes that implements comparable interface
	@Override
	public <U extends Comparable<U>> MetricsManager<U> getMetrics(Class<U> clss, String ColumnName) {
		if(this.clss==null)
		{
			throw new RuntimeException("class object not present");
		}	
		Metamodel metamodel = Metamodel.of(this.clss);
		Map<String,ColumnField> fieldMap = metamodel.getNameFieldMapping();
		System.out.println(fieldMap);
		ColumnField field = fieldMap.get(ColumnName);
		if(field == null)
		{
			throw new RuntimeException("Field not present, check proper case as well");
		}
		
		Map<Class<?>,List<ColumnField>> annotationMap = metamodel.getAnnotationMap();
		System.out.println(annotationMap);
		List<ColumnField> metricFields = annotationMap.get(metric.class);
		List<String> fieldNames =  metricFields.stream().map((cf)->cf.getName()).collect(Collectors.toList());
		if(!fieldNames.contains(field.getName()))
		{
			throw new RuntimeException("Metric annotation not applied.");
		}
		
		

//		if(fieldClass.getCanonicalName()!=clss.getCanonicalName()) 
//		{
//			throw new RuntimeException("Field not matching.");
//		}
		try
		{
			List<T> allTElements = (List<T>)buildInstance(this.clss);
			
			List<U> data = new ArrayList<>();
			field.getField().setAccessible(true);
			for(T t:allTElements)
			{
				data.add((U)field.getField().get(t));
			}
			
			return new MetricsManager<>(data, clss);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public <U extends Comparable<U>, K> GroupMetricsManager<U, T> getGroupMetrics(Class<U> clss, String ColumnName, String methodName, Predicate<T> predicate){
		
		try
		{

			List<T> allTElements = (List<T>) buildInstance(this.clss);
				
				allTElements = allTElements.stream()
											.filter(predicate)
											.collect(Collectors.toList());
				
				return getGroupMetrics(clss, ColumnName, methodName, allTElements);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
		
		
	}
	
	
	
	private <U extends Comparable<U>, K> GroupMetricsManager<U, T> getGroupMetrics(Class<U> clss, String ColumnName, String methodName, List<T> elementsOfT) {
		if(this.clss==null)
		{
			throw new RuntimeException("class object not present");
		}	
		Metamodel metamodel = Metamodel.of(this.clss);
		Map<String,ColumnField> fieldMap = metamodel.getNameFieldMapping();
		System.out.println(fieldMap);
		ColumnField field = fieldMap.get(ColumnName);
		if(field == null)
		{
			throw new RuntimeException("Field not present, check proper case as well");
		}
		
		Map<Class<?>,List<ColumnField>> annotationMap = metamodel.getAnnotationMap();
		System.out.println(annotationMap);
		List<ColumnField> metricFields = annotationMap.get(metric.class);
		List<String> fieldNames =  metricFields.stream().map((cf)->cf.getName()).collect(Collectors.toList());
		if(!fieldNames.contains(field.getName()))
		{
			throw new RuntimeException("Metric annotation not applied.");
		}
		
		

//		if(fieldClass.getCanonicalName()!=clss.getCanonicalName()) 
//		{
//			throw new RuntimeException("Field not matching.");
//		}
		try
		{
			List<T> allTElements;
			
			if(elementsOfT != null && !elementsOfT.isEmpty()) {
				allTElements = elementsOfT;
			} else {
				allTElements = (List<T>) buildInstance(this.clss);
			}
			
			List<U> data = new ArrayList<>();
			field.getField().setAccessible(true);
			for(T t:allTElements)
			{
				data.add((U)field.getField().get(t));
			}
			
			return new GroupMetricsManager<U,T>(data, clss, allTElements, methodName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	

	public <U extends Comparable<U>, K> GroupMetricsManager<U, T> getGroupMetrics(Class<U> clss, String ColumnName, String methodName) {
		
		return getGroupMetrics(clss, ColumnName, methodName, new ArrayList<>());
	}
	
	
	protected List getEntityList(Class<?>[] clss, int index) throws SQLException, Exception {

		return buildInstance(clss[index]);
	}
	
	
	

	
	
	
	@Override
	public List<T> find(Class<T> class1, Predicate<T> predicate) throws Exception {
		/**
		 * this method is used to find object list supplying a predicate condition.
		 * 
		 * 
		 * **/
		Metamodel metamodelT = Metamodel.of(class1);
		String sql=metamodelT.buildSelectRequestAllRows();
		List<T> allTElements = null;
		PreparedStatement statement=prepareStatementWith(sql).getPreparedStatement();
		ResultSet resultSet=statement.executeQuery();
		allTElements=(List<T>)buildInstance(class1, resultSet,metamodelT);
		//allTElements
		//.stream()
		//.filter((t)->predicate.test(t)).forEach(System.out::println);
		
		List<T> list=allTElements
				.stream()
				.filter((t)->predicate.test(t))
				.collect(Collectors.toList());

		return list;
	}
	
	
	public <U>List<U> join(Object t,Class<U> classU)
	{
		List<U> list=null;
		
		
	Metamodel metamodelT = Metamodel.of(t.getClass());
	Metamodel metamodelU = Metamodel.of(classU);
	
	List <Class<?>> classes = List.of(t.getClass());
	for (Class<?> clss : classes) {
		Method[] methods= clss.getDeclaredMethods();
		for (Method method : methods) {
			Provides provides = method.getAnnotation(Provides.class);
	
			if(provides !=null)
			{
				//System.out.println("provides here");
				Class<?> returnType=method.getReturnType();

				
				Supplier<?> supplier = () -> {
					try {
						if (!Modifier.isStatic(method.getModifiers())) {
							
							return method.invoke(t);
						} else {
							return method.invoke(null);
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				};
				
				registry.put(returnType, supplier);
			}
		}
		
		
		PrimaryKeyField primaryKeyU = metamodelU.getPrimaryKey();
		List<ColumnField> temp = metamodelU.getAnnotationMap().get(Inject.class);
		if(temp !=null)
		{
			
		
		
		int j=temp.size();
		ColumnField[] injectArray= new ColumnField[j];
		List<ColumnField> injectList=temp.stream()
							.filter((f)->f.getField().getAnnotation(Inject.class).classname().equals(clss.getName())).collect(Collectors.toList());
		int n=injectList.size();
		

		Object[] valueList=new Object[n];
		ColumnField[] fieldist=new ColumnField[n];
		int i=0;
		for(ColumnField field:injectList)
		{Object value=null;
			try {

				Class<?> returnType = field.getField().getType();
				//System.out.println("field Return Type:::: "+returnType);
				value = registry.get(returnType).get();
				//System.out.println("Query Key:---------:-"+registry.get(returnType).get());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				try {
				//Object uObject =  classU.getConstructor().newInstance();
				//field.getField().setAccessible(true);
				//field.getField().set(uObject, value);
				fieldist[i]=field;
				valueList[i]=value;
				
				//list= findGeneric(classU, field,value);
				
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				i++;
			
		}
		try {
			list=findGenericMultipleConditions(classU, fieldist, valueList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
		else
		{
			if(primaryKeyU.getField().getAnnotation(Inject.class)!=null)
			{
				Inject inject = primaryKeyU.getField().getAnnotation(Inject.class);
				
				Object value=null;
				try {

					Class<?> returnType = primaryKeyU.getField().getType();
					//System.out.println("field Return Type:::: "+returnType);
					value = registry.get(returnType).get();
					list= find(classU, value, primaryKeyU.getName());
					//find(classU, primaryKeyU.getName(), value);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}
	
	}
	
	
	
	
		
		
		return list;
		
		
	} // Join 
	
	
	protected Object readObjectFromFile(String filepath) {
		 
        try {
 
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
 
            Object obj = objectIn.readObject();
            objectIn.close();
            fileIn.close();
            return obj;
 
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

	protected List findGeneric(Class<?> clss, ColumnField queryKey, Object value) throws Exception {
		return findGenericMultipleConditions(clss, new ColumnField[] {queryKey}, new Object[] {value});
	} // find
	
	protected void saveToFile(T t, String primaryKeyValue, ColumnField cf, String path) throws IllegalAccessException {
		try {
			FileOutputStream fos = new FileOutputStream(path+t.getClass().getName()+File.separator+cf.getName()+"_"+primaryKeyValue+".ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			// write object to file
			oos.writeObject( cf.getField().get(t));
			System.out.println("Done");
			// closing resources
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateReverseDependency(T t){
		List <Class<?>> classes = List.of(t.getClass());
		for (Class<?> clss : classes) {
			Field[] fields= clss.getDeclaredFields();
			for (Field field : fields) {
				update u = field.getAnnotation(update.class);
				System.out.println(u);
				if(u !=null)
				{
					
					field.setAccessible(true);
					
					Supplier<?> supplier = () -> {
						try {
							Object obj=null;
							
							obj = field.get(t);
							
							
							
							
							Class cl=getClass().getClassLoader().loadClass(u.classname());
							
							Object dependentObject= classToObjGenericWrap(cl).find(cl, obj);
							
							Metamodel metamodelDependentClass = Metamodel.of(cl);
							Map<Class<?>,List<ColumnField>> annotationMap = metamodelDependentClass.getAnnotationMap();
							List<ColumnField> listLink=annotationMap.get(link.class);
							
							for(ColumnField cField:listLink)
							{
								if(dependentObject!=null)
								{
									Field linkField=cField.getField();
									linkField.setAccessible(true);
									
									return linkField.get(dependentObject);
								}
							}

	
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					return null;
					};
					//supplier.get();
					registrySearch.put(u.id(), supplier);
				}
			}
			System.out.println(registrySearch);
			/**starts updating Dependency**/
			
			for(Method method:clss.getDeclaredMethods())
			{
				try
				{
				setter setterAnnotation = method.getAnnotation(setter.class);
				//System.out.println("Person Fields:-----:-"+field.getName()+"<<<<>>>>"+updateAnnotation);
				if(setterAnnotation !=null)
				{
					
					Class<?> returnType = method.getReturnType();
					//System.out.println("field Return Type:::: "+returnType.getName());
					
					Object value ;
					if(registrySearch.get(setterAnnotation.id())==null)
						value=null;
					else
					{
						value = registrySearch.get(setterAnnotation.id()).get();
						method.invoke(t, new Object[] {value});
					}
					//System.out.println("Query Key:---------:-"+registry.get(returnType).get());

				}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	@Override
	public void searchAndUpdateDependency(T t) {
		// TODO Auto-generated method stub
		List <Class<?>> classes = List.of(t.getClass());
		
		for (Class<?> clss : classes) {
			Metamodel metamodelT=Metamodel.of(clss);
			Method[] methods= clss.getDeclaredMethods();
			for (Method method : methods) {
				search s = method.getAnnotation(search.class);
				System.out.println(s);
				if(s !=null)
				{
					//System.out.println("provides here");
					Class<?> returnType=method.getReturnType();

					
					Supplier<?> supplier = () -> {
						try {
							Object obj=null;
							if (!Modifier.isStatic(method.getModifiers())) {
								
								obj=method.invoke(t);

							} else {
								obj=method.invoke(null);
							}
							Class cl=getClass().getClassLoader().loadClass(s.classname());
							
							Metamodel metamodelInDependentClass = Metamodel.of(cl);
							Map<Class<?>,List<ColumnField>> annotationMap = metamodelInDependentClass.getAnnotationMap();
							List<ColumnField> listLink=annotationMap.get(link.class);
							List dependentListObject=null;
							System.out.println("Object to search--------->"+listLink);
							for(ColumnField cField:listLink)
							{
								if(obj!=null)
								{
									dependentListObject = findGeneric(cl, cField, obj);
								}
							}
							Object linkObject=null;
							if(dependentListObject!=null && !dependentListObject.isEmpty())
							{
								 linkObject = dependentListObject.get(0);
								 
								 Field field=metamodelInDependentClass.getPrimaryKey().getField();
								 field.setAccessible(true);
								 //System.out.println("returnPrimaryValue:::::::::"+field.get(linkObject));
								 return field.get(linkObject);
								
							}
							
							
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					return null;
					};
					//supplier.get();
					registrySearch.put(s.id(), supplier);
				}
			}
			System.out.println(registrySearch);
			/**starts updating Dependency**/
			List<ColumnField> updateAnnotationFieldList =  metamodelT.getAnnotationMap().get(update.class);
			
			for(ColumnField field:updateAnnotationFieldList)
			{
				try
				{
					update updateAnnotation = field.getField().getAnnotation(update.class);
					Class<?> returnType = field.getField().getType();
					//System.out.println("field Return Type:::: "+returnType.getName());
					
					Object value ;
					if(registrySearch.get(updateAnnotation.id())==null)
						value=null;
					else
						value = registrySearch.get(updateAnnotation.id()).get();
					Field updateField=field.getField();
					updateField.setAccessible(true);//setField(field);
					if(value!=null)
					updateField.set(t, value);
					//System.out.println("Query Key:---------:-"+registry.get(returnType).get());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
	}// updateDependency
	
	public List findGenericMultipleConditions(Class<?> clss, ColumnField[] queryKey, Object[] value) throws Exception {
		
		Metamodel metamodel=Metamodel.of(clss);
		String sql =metamodel.buildSelectRequest(queryKey);
		System.out.println("Generic Request sql::::"+sql);
		try(PreparedStatement statement=prepareStatementWith(sql).andQueryKeyWithMultipleConditions(value);
		ResultSet resultSet=statement.executeQuery();)
		{
		return buildInstance(clss, resultSet,metamodel);
		}
		
	}
	protected PreparedStatementWrapperChild prepareStatementWith(String sql) throws SQLException {
		// TODO Auto-generated method stub
				Connection con = null;	
				if(connection != null)
					con = connection.borrowObject();	
				else
					con = conn;
				PreparedStatement preparedStatement= con.prepareStatement(sql);
				if(connection != null)
					connection.returnObject(con);
				return new PreparedStatementWrapperChild(preparedStatement);
				
				
				}
	
	
	
	@Override
	public List find(Class<T> clss, String columnName, Object columnValue) throws Exception {
		// TODO Auto-generated method stub

		Metamodel metamodel=Metamodel.of(clss);
		if(metamodel.getNameFieldMapping().get(columnName)==null)
			{
			System.out.println("Column Name not found in table");
			return null;}
		String sql =metamodel.buildSelectRequestGenericColumnFilter(columnName);
		try(PreparedStatement statement=prepareStatementWith(sql).andQueryKey(columnValue);
		ResultSet resultSet=statement.executeQuery();)
		{
		return buildInstance(clss, resultSet,metamodel);
		}
		
	
		
		
	}
	
	
	
	public List find(Class<?> clss,  Object columnValue, String columnName) throws Exception {
		// TODO Auto-generated method stub

		Metamodel metamodel=Metamodel.of(clss);
		if(metamodel.getNameFieldMapping().get(columnName)==null)
			{
			System.out.println("Column Name not found in table");
			return null;}
		String sql =metamodel.buildSelectRequestGenericColumnFilter(columnName);
		try(PreparedStatement statement=prepareStatementWith(sql).andQueryKey(columnValue);
		ResultSet resultSet=statement.executeQuery();)
		{
		return buildInstance(clss, resultSet,metamodel);
		}
		
	
		
		
	}
	
	

	Stack<List<Wrapper>> stack = new Stack<>();
	@Override
	public Stack<List<Wrapper>> returnList() {
		while(!stack.isEmpty())
		{
			returnStack.push(stack.pop());
		}
		
		return returnStack;
	}
	
	
	@Override
	public List<Wrapper> returnSingleEntityObjects(Class<?> clss) {
		// TODO Auto-generated method stub
		
		return wrapperListToClassMap.get(clss);
	}
	
	Stack<List<Wrapper>> returnStack = new Stack<>();
	Map<Class<?>,List<Wrapper>> wrapperListToClassMap = new HashMap();
	@Override
	public EntityManager<T> link(Class<?> clss) {
		Metamodel metamodel = Metamodel.of(clss);
		List<Wrapper> list = null;
		if(stack.isEmpty())
		{
			try {
				list = buildInstance(clss);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<Wrapper> wrapperList = new ArrayList<>();
			for(Object obj:list)
			{
				Wrapper wrap = Wrapper.wrapIt(obj);
				wrap.setWrapList(wrapperList);
				wrapperList.add(wrap);
			}
			stack.push(wrapperList);
			wrapperListToClassMap.put(clss, wrapperList);
			return this;
		}
			list = stack.peek();
			//stack.push(list);
			List<Wrapper> childList = new ArrayList();
			for(Wrapper obj:list)
			{
				List temp = join(obj.getData(),clss);
				List<Wrapper> wrapper = new ArrayList<>();
				for(Object childObj:temp)
				{
					Wrapper wrap = Wrapper.wrapIt(childObj);
					wrap.setParent(obj);
					wrap.getParent().childCount++;
					wrap.setWrapList(childList);
					wrapper.add(wrap);
				}
				childList.addAll(wrapper);
			}
			wrapperListToClassMap.put(clss, childList);
			stack.push(childList);
			
			
			
			
			return this;
	}
	
//	private <U>EntityManager<U> wrap(Stack<List<Wrapper>> stack,Connection con, Map<Class<?>,List<Wrapper>> wrapperListToClassMap)
//	{
//		return new RelationJoinManager<U>(stack,con, wrapperListToClassMap);
//	}
	
	
	protected <U>EntityManager<U> wrap(ObjectPool<Connection> con)
	{
		return new RelationJoinManager<U>(null,con, null);
	}
	
	private RelationJoinManager(Stack<List<Wrapper>> stack,ObjectPool<Connection> con, Map<Class<?>,List<Wrapper>> wrapperListToClassMap) {
		super();
		this.stack = stack;
		this.connection=con;
		this.wrapperListToClassMap=wrapperListToClassMap;
	}
	

protected <U>EntityManager<U> classToObjGenericWrap(Class<U> clss)
{
	
	return new RelationJoinManager(stack,connection, wrapperListToClassMap);
}




@Override
	public <U>EntityManager<U> connect(Class<U> clss, JoinType NAME) {
		Metamodel metamodel = Metamodel.of(clss);
		List<Wrapper> list = null;
		if(stack.isEmpty())
		{
			try {
				list = buildInstance(clss);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			List<Wrapper> wrapperList = new ArrayList<>();
			for(Object obj:list)
			{
				Wrapper wrap = Wrapper.wrapIt(obj);
				wrap.setJoin(NAME);
				wrap.setWrapList(wrapperList);
				wrapperList.add(wrap);
			}
			stack.push(wrapperList); // objects on first level class
			wrapperListToClassMap.put(clss, wrapperList);
			return classToObjGenericWrap(clss);
		}
			list = stack.peek();
			//stack.push(list);
			List<Wrapper> childList = new ArrayList();
			List<Wrapper> objToRemove = new ArrayList();
			for(Wrapper obj:list)
			{
				List temp = join(obj.getData(),clss);
				if(temp.isEmpty() && NAME==JoinType.INNER)
				{
					objToRemove.add(obj);
				}
				List<Wrapper> wrapper = new ArrayList<>();
				for(Object childObj:temp)
				{
					Wrapper wrap = Wrapper.wrapIt(childObj);
					wrap.setJoin(NAME);
					wrap.setParent(obj);
					wrap.setWrapList(childList);
					wrapper.add(wrap);
				}
				obj.childCount = temp.size(); // no of children = childlist size
				obj.setConnections(wrapper);
				childList.addAll(wrapper);
			}
			
			if(NAME==JoinType.INNER)
			{
				list.removeAll(objToRemove);
			}
			wrapperListToClassMap.put(clss, childList);
			stack.push(childList);
			
			
			
			
			return classToObjGenericWrap(clss);
}

public List<Map<String, Object>> structuredData(JoinType joinType, Class<?>... clses){
	
	List<Map<String, Object>> returnList = new ArrayList<>();
	if(joinType==null) {
		joinType = JoinType.LEFT;
	}
	
	
	
	if(clses==null || clses.length==0) {
		return returnList;
	}
	
	EntityManager<?> tempManager = this.connect(clses[0], joinType);
	
	for(int i=1; i<clses.length; i++) {
		tempManager = tempManager.connect(clses[i], joinType);
	}
	
	Stack<List<Wrapper>> stack = tempManager.returnList();
	
	List<Wrapper> levelFirst = stack.pop();
	levelFirst.forEach(wrap->{
		List<Wrapper> accumulator = new ArrayList<>();
		accumulator.add(wrap);
		recurseAndPrepareStructuredData(clses, 0, wrap, returnList, accumulator);
	
	});
	return returnList;
	
	
	
}


private void recurseAndPrepareStructuredData(Class<?>[] clses,
											int idx,
											Wrapper current,
											List<Map<String, Object>> returnList,
											List<Wrapper> accumulator) {
	if(current.getConnections()!=null) {
		current.getConnections().forEach((connection)->{
			List<Wrapper> forwardAccumulator = new ArrayList<>();
			forwardAccumulator.addAll(accumulator);
			forwardAccumulator.add(connection);
			
			recurseAndPrepareStructuredData(clses, idx+1, connection, returnList, forwardAccumulator);
			
			
			});
	}
	
	Map<String, Object> data = new HashMap<>();
	int classIndex = 0;
	for(Wrapper wrap:accumulator) {
		Object object = wrap.getData();
		Class<?> objectClass = clses[classIndex];
		
		Field[] fields = objectClass.getDeclaredFields();
		
		for(Field field:fields) {
			
			if(field.getAnnotation(DisplayName.class)!=null) {
				field.setAccessible(true);
				try {
					data.put(field.getAnnotation(DisplayName.class)!=null?
													field.getAnnotation(DisplayName.class).name():
													field.getName(), field.get(object));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					System.out.println(object.getClass()+"<<<<>>>>"+objectClass);
					
					//e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}
		
		classIndex++;
	}
	
	returnList.add(data);


}
	
	
	@Override
	public EntityManager<T> filter(Predicate<T> predicate) {
		if(stack.isEmpty())
			return this;
		List<Wrapper> list = stack.peek();
		List<Wrapper> eliminatedObjects = new ArrayList<>();
		//System.out.println(predicate.test(list.get(0)));
		for(Wrapper obj:list)
		{
			if(!predicate.test((T) obj.getData()))
			{
				eliminatedObjects.add(obj);
			}
			}
		//list.stream().filter((o)->!predicate.test(o)).forEach((o)->list.remove(o));
		eliminatedObjects.stream().forEach((w)->testAndRemoveHierarchy(w, false));
		list.removeAll(eliminatedObjects);
		return this;
		
	}
	
	@Override
	public EntityManager<T> apply(Consumer<T> con)
	{
		if(stack.isEmpty())
			return this;
		List<Wrapper> list = stack.peek();
		//List<Wrapper> temp = new ArrayList<>();
		//System.out.println(predicate.test(list.get(0)));
		for(Wrapper obj:list)
		{
			con.accept((T)obj.getData());
			}
		return this;
	}
	
	
	public void testAndRemoveHierarchy(Wrapper wrap, boolean recursiveCall)
	{
		if(wrap.getParent()==null && recursiveCall)
		{
			wrap.getWrapList().remove(wrap);
			return;
		}else
		if(wrap.getParent()==null)
			return;
		if(wrap.getParent()!=null && (wrap.getParent().getCount()>1 || wrap.getJoin() == JoinType.LEFT)) // no need to remove ancestor in case join is left
		{
			wrap.getParent().childCount--;
			wrap.getParent().getConnections().remove(wrap);
			wrap.setParent(null);
			wrap.getWrapList().remove(wrap);
			return;
		}
		testAndRemoveHierarchy(wrap.getParent(), true);
		wrap.getParent().childCount--;
		wrap.getParent().getConnections().remove(wrap);
		wrap.setParent(null);
		wrap.getWrapList().remove(wrap);
	}
	
	@Override
	public List<T> buildInstances(Class<T> clss) {
		// TODO Auto-generated method stub
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
	
}
