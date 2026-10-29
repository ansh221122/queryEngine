package beanmanager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import reflection.utility.EntityManager;
import reflection.utility.Inject;
import reflection.utility.IntegerFactory;
import reflection.utility.ManagedEntityManager;
import reflection.utility.Person;
import reflection.utility.Provides;
import reflection.utility.uniqueIdUtil;
import provider.H2ConnectionProvider;

public class BeanManager {

	private static BeanManager instance=null;
	private static BeanManager instanceWithPath=null;
	//private Class<?> p;
	//private Class<p> providerClass=new Class<p>(null, p); 


	private Class<?> clss;
	private Map<Class<?>, Supplier<?>> registry=new HashMap();
	
	
	public static BeanManager getManagerInstance(Class<?> clss) {
		// TODO Auto-generated method stub
		if(instance==null)
		{
			instance=new BeanManager(clss);
		}
		//clss=clss;
		return instance;
	}
	
	
	public static BeanManager getManagerInstance(Class<?> clss,Object path) {
		// TODO Auto-generated method stub
		if(path == null)
		{
			return getManagerInstance(clss);
		}
		if(instanceWithPath==null)
		{
			instanceWithPath=new BeanManager(clss,path);
		}
		//clss=clss;
		return instanceWithPath;
	}
	

	
	private BeanManager(Class<?> clses, Object path)
	{
		
		this.clss=clses;
		List <Class<?>> classes = List.of(clses);
		for (Class<?> clss : classes) {
			Method[] methods= clss.getDeclaredMethods();
			for (Method method : methods) {
				Provides provides = method.getAnnotation(Provides.class);
		
				if(provides !=null)
				{
					Class<?> returnType=method.getReturnType();
//					Supplier<?> supplier = () -> {
//						Connection connection=null;
//						try {
//						if(!Modifier.isStatic(method.getModifiers()))
//						{
//						Object o = clss.getConstructor().newInstance();
//						connection=(Connection)method.invoke(o);
//						}
//						else
//						{
//							connection=(Connection)method.invoke(null);
//						}
//						}
//						catch(Exception e)
//						{
//							throw new RuntimeException(e);
//						}
//						//return clss;
//						return connection;
//						
//						
//					};
					
					Supplier<?> supplier = () -> {
						try {
							System.out.println("reached here::"+path);
							if(path.getClass() == returnType) // to override the default configuration
								return path;
							if (!Modifier.isStatic(method.getModifiers())) {
								Object o = clss.getConstructor().newInstance();
								return method.invoke(o);
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
		}
		
	}
	
	
	private BeanManager(Class<?> clses)
	{
		this.clss=clses;
		List <Class<?>> classes = List.of(clses);
		for (Class<?> clss : classes) {
			Method[] methods= clss.getDeclaredMethods();
			for (Method method : methods) {
				Provides provides = method.getAnnotation(Provides.class);
		
				if(provides !=null)
				{
					Class<?> returnType=method.getReturnType();
//					Supplier<?> supplier = () -> {
//						Connection connection=null;
//						try {
//						if(!Modifier.isStatic(method.getModifiers()))
//						{
//						Object o = clss.getConstructor().newInstance();
//						connection=(Connection)method.invoke(o);
//						}
//						else
//						{
//							connection=(Connection)method.invoke(null);
//						}
//						}
//						catch(Exception e)
//						{
//							throw new RuntimeException(e);
//						}
//						//return clss;
//						return connection;
//						
//						
//					};
					
					Supplier<?> supplier = () -> {
						try {
							if (!Modifier.isStatic(method.getModifiers())) {
								Object o = clss.getConstructor().newInstance();
								return method.invoke(o);
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
		}
		
	}

	public <T extends EntityManager<T>>T getInstance(Class<T> clss) {
		try {
			System.out.println("instance cls-->"+clss);
			boolean setFlag= false;
			boolean setUniqueIdUtil = false;
			T t=clss.getConstructor().newInstance();
			for(Class<T> clses: List.of(clss))
			{
				System.out.println("list classes ::::"+clses);
				Field[] fields=clses.getDeclaredFields();
				setFlag = injectFieldDependency(setFlag, t, fields);
			
				try {
					setUniqueIdUtil = setUniqueIdUtil(setUniqueIdUtil, t, fields);
					
				}
				catch(Exception e)
				{
					System.err.println("this class do not have uniqueIdUtil object");
					e.printStackTrace();
				}
			
			Class<?> prevClass=clses;
			while(prevClass.getSuperclass()!=Object.class)
			{
				Class<?> currClass=prevClass.getSuperclass();
				fields  = currClass.getDeclaredFields();

				prevClass=currClass;
				setFlag = injectFieldDependency(setFlag, t, fields);
				
			}
			
			prevClass=clses;
			while(!setUniqueIdUtil && prevClass.getSuperclass()!=Object.class)
			{		try {
							Class<?> currClass=prevClass.getSuperclass();
							fields  = currClass.getDeclaredFields();
							prevClass = currClass;
				
							setUniqueIdUtil = setUniqueIdUtil(setUniqueIdUtil, t, fields);
						
					}
					
					catch(Exception e)
					{
						System.err.println("this class do not have uniqueIdUtil object");
						e.printStackTrace();
					}
			
			}
			}
		return t;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		} 
		
		//return null;
	}


	protected <T> boolean setUniqueIdUtil(boolean setUniqueIdUtil, T t, Field[] fields)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		for(Field field:fields)
		{
			System.out.println("Field Name:"+field.getName());
			
			uniqueIdUtil idUtil = field.getAnnotation(uniqueIdUtil.class);
			System.out.println("annotation:"+idUtil);
			if(idUtil !=null)
			{
				Class<? extends IntegerFactory> idUtilClass =null;
				Object integerFactory = null;
				if(registry.containsKey(IntegerFactory.class))
				{
					System.out.println("<<<<<<<<<<<<<<<We get the class from provider>>>>>>>>>>>>>>>>");
					//idUtilClass = (Class<? extends IntegerFactory>)registry.get(IntegerFactory.class).get();
					integerFactory = registry.get(IntegerFactory.class).get();
				}
				else
				{
					idUtilClass = idUtil.uniqueIdUtilClass();
					integerFactory = idUtilClass.getConstructor().newInstance();
				}
				field.setAccessible(true);
				field.set(t, integerFactory);
				setUniqueIdUtil = true;
				break;
			}
		}
		return setUniqueIdUtil;
	}


	protected <T> boolean injectFieldDependency(boolean setFlag, T t, Field[] fields) throws IllegalAccessException {
		for (Field field : fields) {
			System.out.println("field Names :-----:-"+field.getName());
			Inject inject=field.getAnnotation(Inject.class);
			if(inject!=null)
			{
				
				Class<?> injectFieldReturnType = field.getType();
				Supplier<?> supplier = registry.get(injectFieldReturnType);
				if(supplier !=null)
				{
					Object objectToInject = supplier.get();
					field.setAccessible(true);
					field.set(t, objectToInject);
					setFlag=true;
				}
			
			}
		}
		return setFlag;
	}
	
	
	
	private String genericClassField = "clss";
	public <U>EntityManager<U> getInstance(Class<U> clss,Class<? extends EntityManager> obj) {
		try {
			System.out.println("instance cls-->"+clss);
			boolean setFlag= false;
			boolean setClssField = false;
			boolean setUniqueIdUtil = false;
			EntityManager<U> em=obj.getConstructor().newInstance();
			for(Class<?> clses: List.of(obj))
			{
				System.out.println("list classes ::::"+clses);
			Field[] fields=clses.getDeclaredFields();
			
		try {
				Field clssField = clses.getDeclaredField(genericClassField);
				
				if(clssField!=null)
				{
					clssField.setAccessible(true);
					clssField.set(em, clss);
					setClssField=true;
				}
				
			}
			catch(Exception e)
			{
				System.out.println("this class do not have field clss");
			}
		
		
		try {
			setUniqueIdUtil = setUniqueIdUtil(setUniqueIdUtil, em, fields);
			
		}
		catch(Exception e)
		{
			System.err.println("this class do not have uniqueIdUtil object");
		}
			
			setFlag = injectFieldDependency(setFlag, em, fields);

			Class<?> prevClass=clses;
			while(!setClssField && prevClass.getSuperclass()!=Object.class)
			{
				Class<?> currClass=prevClass.getSuperclass();
				//fields  = currClass.getDeclaredFields();
				
				try {
					Field clssFieldSuper = currClass.getDeclaredField("clss");
					if(clssFieldSuper!=null)
					{
						clssFieldSuper.setAccessible(true);
						clssFieldSuper.set(em, clss);
						setClssField=true;
					}
				}
				catch(Exception e)
				{
					System.out.println("this class do not have clss field.");
				}
				
				
				prevClass=currClass;
			}
			
			prevClass=clses;
			while(!setFlag && prevClass.getSuperclass()!=Object.class)
			{
				Class<?> currClass=prevClass.getSuperclass();
				fields  = currClass.getDeclaredFields();
				prevClass = currClass;
				setFlag = injectFieldDependency(setFlag, em, fields);

				
			}
			
			prevClass=clses;
			while(!setUniqueIdUtil && prevClass.getSuperclass()!=Object.class)
			{		try {
							Class<?> currClass=prevClass.getSuperclass();
							fields  = currClass.getDeclaredFields();
							prevClass = currClass;
							setUniqueIdUtil = setUniqueIdUtil(setUniqueIdUtil, em, fields);
						
					}
					
					catch(Exception e)
					{
						System.err.println("this class do not have uniqueIdUtil object");
					}
			
			}
			}
		return em;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return null;
	}
	
}
