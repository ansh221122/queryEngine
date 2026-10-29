package reflection.utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.ColumnField;
import util.JsonMetamodel;
import util.Metamodel;
import util.PrimaryKeyField;
import util.XMLMetamodel;

public class XMLEntityManager<T>  extends RelationJoinManager<T>{

	@Inject(classname="")
	String path;
    DocumentBuilderFactory dbFactory;
    DocumentBuilder dBuilder;
    Set uniqueIDs;
	
    
    @Override
	protected List findGeneric(Class<?> clss, ColumnField queryKey, Object value) throws Exception {
    	return findGenericMultipleConditions(clss, new ColumnField[] {queryKey}, new Object[] {value});
    } // find
 @Override
	protected List getEntityList(Class<?>[] clss, int index) throws SQLException, Exception {
		XMLMetamodel metamodelT = XMLMetamodel.of(clss[index]);		
		List allTElements = null;
		allTElements=buildInstance(clss[index], metamodelT);
		return allTElements;
	}
 
 
 List buildInstance(Class<?> class1,XMLMetamodel metamodel ) throws Exception
 {
	 List list = new ArrayList();
	 File file = new File(metamodel.getXMLFilePath(path));
	Document doc = dBuilder.parse(file);
	doc.getDocumentElement().normalize();  
	System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
	NodeList nodeList = doc.getElementsByTagName(class1.getSimpleName());
	for(int i=0;i<nodeList.getLength();i++)
	{
		Node node = nodeList.item(i);
		Element elem = (Element)node;
		Object obj=buildInstance(class1, elem);
		updateInjectListFields(metamodel, obj);
				
		list.add(obj);
	}
	return list;
 }
 
 
 
 @Override
protected List buildInstance(Class clss) throws SQLException, Exception {
		XMLMetamodel metamodelT = XMLMetamodel.of(clss);		
		List allTElements = null;
		allTElements=buildInstance(clss, metamodelT);
		return allTElements;
	}
 
 
    
    
	public XMLEntityManager() {
		super();
        dbFactory = DocumentBuilderFactory.newInstance();

        try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}
	
    private static Node getElements(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

    @Override
    public List<T> importFromCSV(Class<T> clss,String filePath) {
    	File file = new File(filePath);
    	String[] columnNames=null;
    	String[] data;
    	List<T> returnList = new ArrayList<>();
    	List<String> columnValues = null;
    	if(!file.exists())
    		return null;
    	if(!file.getName().toLowerCase().endsWith(".csv"))
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
					if(data.length!=columnNames.length)
						continue;
					for(int j=0;j<dataValues.size();j++)
					{
						keyValueMap.put(columnValues.get(j), dataValues.get(j));
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
	public List<T> importFromArray(Class<T> clss, String[][] array) {
    	//File file = new File(filePath);
    	String[] columnNames=null;
    	String[] data;
    	List<T> returnList = new ArrayList<>();
    	List<String> columnValues = null;
    	if(array==null || array.length==0 || array[0].length==0)
    		return null;

    	try {
			
			String line="";
			int i=0;
			while(i<array.length)
			{
				if(i==0)
				{
					columnNames=array[i];
					columnValues=Arrays.stream(columnNames).collect(Collectors.toList());
					
				}
				else
				{
					
					data=array[i];
					List<String> dataValues=Arrays.stream(data).collect(Collectors.toList());
					Map<String,String> keyValueMap=new HashMap();
					T t=null;
					if(data.length!=columnNames.length)
						continue;
					for(int j=0;j<dataValues.size();j++)
					{
						keyValueMap.put( columnValues.get(j), dataValues.get(j));
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
	public boolean inspectUnique(T t) throws Exception {
		// TODO Auto-generated method stub
		
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
		Class<T> clss = (Class<T>) t.getClass();
		Map<Class<?>,List<ColumnField>> annotationMap=metamodel.getAnnotationMap();
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
	public void persist(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
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
			e.printStackTrace();
			
		}
		File inputFile = new File(metamodel.getXMLFilePath(path));
		
		
		if(!inputFile.exists())
		{
			createFile((Class<T>)t.getClass());
			inputFile = new File(metamodel.getXMLFilePath(path));
			

		}
		updateUniqueID(t, inputFile);
		
		try {
			Document doc = dBuilder.parse(inputFile);
			Node clss = doc.getFirstChild();
            
			Node tNode = getTNode(doc,t);
			clss.appendChild(tNode);
			//doc.appendChild(clss);
			//Object ob = clss.getConstructor().newInstance();
            savetoFile(metamodel, doc);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void savetoFile(XMLMetamodel metamodel, Document doc)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		//for pretty print
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		//transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(2)); 
		DOMSource source = new DOMSource(doc);
		StreamResult console = new StreamResult(System.out);
		StreamResult file = new StreamResult(new File(metamodel.getXMLFilePath(path)));
		transformer.transform(source, console);
		transformer.transform(source, file);
		System.out.println("DONE");
	}

	private void updateUniqueID(T t, File file) {
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
		//File file = new File(metamodel.getXMLFilePath(path));
		try {
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();  
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
			NodeList nodeList = doc.getElementsByTagName(t.getClass().getSimpleName());
			if(nodeList.getLength()==0)
			{
				Integer newID=RandomIntegerFactory.getRandomInteger();

				Field field=metamodel.getPrimaryKey().getField();
				field.setAccessible(true);
				field.set(t,newID);
				return;
			}
			uniqueIDs =new HashSet<String>();
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			String primaryKeyValue= elem.getAttribute(metamodel.getPrimaryKey().getName());
			uniqueIDs.add(primaryKeyValue);
			
			}
			
			Integer newID=RandomIntegerFactory.getRandomInteger();
			while(uniqueIDs.contains(newID))
			{
				newID=RandomIntegerFactory.getRandomInteger();
			}
			Field field=metamodel.getPrimaryKey().getField();
			field.setAccessible(true);
			field.set(t,newID);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private Node getTNode_temp(Document doc, T t) {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
		Element tNode = doc.createElement(t.getClass().getSimpleName());
		PrimaryKeyField primaryField =metamodel.getPrimaryKey();
		primaryField.getField().setAccessible(true);
		try {
			String primaryKeyValue = primaryField.getField().get(t).toString();
			tNode.setAttribute(metamodel.getPrimaryKey().getName(), primaryField.getField().get(t).toString());
			
			for(ColumnField cf:metamodel.getColumns())
			{
				try
				{
				cf.getField().setAccessible(true);
				Node cfElem =null;
				Class<?> cfType = cf.getField().getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					File dir_class= new File(path+t.getClass().getName());
					if(!dir_class.exists())
					{
					
					dir_class.mkdir();
					}
					
					saveToFile(t, primaryKeyValue, cf,path);
					cfElem=getElements(doc,  cf.getName(), t.getClass().getName()+File.separator+cf.getName()+"_"+primaryKeyValue);
				}
					else	
				{cfElem=getElements(doc,  cf.getName(),(String) cf.getField().get(t).toString());}
				tNode.appendChild(cfElem);
				}catch(NullPointerException e)
				{
					System.out.println("the value for column Field <"+cf.getName()+"> is null, Please provide default value in class to avoid this error.");
				}
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return tNode;
	}

	
	
	private Node getTNode(Document doc, T t) {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
		Element tNode = doc.createElement(t.getClass().getSimpleName());
		PrimaryKeyField primaryField =metamodel.getPrimaryKey();
		primaryField.getField().setAccessible(true);
		try {
			String primaryKeyValue = primaryField.getField().get(t).toString();
			tNode.setAttribute(metamodel.getPrimaryKey().getName(), primaryField.getField().get(t).toString());
			
			for(ColumnField cf:metamodel.getColumns())
			{
				try
				{
				cf.getField().setAccessible(true);
				Node cfElem =null;
				Class<?> cfType = cf.getField().getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{

					Node genericNode=getTNodeGeneric(doc, cf.getField().get(t), cf);
					tNode.appendChild(genericNode);
				}
				else{
					
					cfElem=getElements(doc,  cf.getName(),(String) cf.getField().get(t).toString());
					
					tNode.appendChild(cfElem);
				}
				}catch(NullPointerException e)
				{
					e.printStackTrace();
					System.out.println("the value for column Field <"+cf.getName()+"> is null, Please provide default value in class to avoid this error.");
				}
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return tNode;
	}
	
	
	
	private Node getTNodeGeneric(Document doc, Object t, ColumnField cfGeneric) {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
		Element tNode = doc.createElement(cfGeneric.getName());
//		PrimaryKeyField primaryField =metamodel.getPrimaryKey();
//		primaryField.getField().setAccessible(true);
		try {
//			String primaryKeyValue = primaryField.getField().get(t).toString();
//			tNode.setAttribute(metamodel.getPrimaryKey().getName(), primaryField.getField().get(t).toString());
			
			for(ColumnField cf:metamodel.getColumns())
			{
				try
				{
				cf.getField().setAccessible(true);
				Node cfElem =null;
				Class<?> cfType = cf.getField().getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{

					Node genericNode=getTNodeGeneric(doc, cf.getField().get(t), cf);
					tNode.appendChild(genericNode);
				}
					else	
				{
						cfElem=getElements(doc,  cf.getName(),(String) cf.getField().get(t).toString());
						tNode.appendChild(cfElem);
				}
				}catch(NullPointerException e)
				{
					System.out.println("the value for column Field <"+cf.getName()+"> is null, Please provide default value in class to avoid this error.");
				}
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return tNode;
	}




	@Override
	public void update(T t) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
		Field primaryKeyField= metamodel.getPrimaryKey().getField();
		primaryKeyField.setAccessible(true);
		Object primaryKey=primaryKeyField.get(t);
		File file = new File(metamodel.getXMLFilePath(path));
		try {
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();  
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
			NodeList nodeList = doc.getElementsByTagName(t.getClass().getSimpleName());

			
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			String primaryKeyValue= elem.getAttribute(metamodel.getPrimaryKey().getName());
			//System.out.println(">>>>>>>>"+primaryKeyValue.toString()+"<<<<<<<<>>>>>>"+primaryKey);
			if(primaryKeyValue.trim().equals(primaryKey.toString()))
			{
				//System.out.println(">>>>>>>>"+primaryKeyValue.toString()+"<<<<<<<<>>>>>>"+primaryKey);	
				updateObjectInXML(t, elem, metamodel);
				break;
			}
			}
			
			 savetoFile(metamodel, doc);
			
			
		}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	@Override
	public T find(Class<T> class1, Object primaryKey) throws Exception {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(class1);
		
		File file = new File(metamodel.getXMLFilePath(path));
		if(!file.exists())
		{
			return null;
		}
		try {
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();  
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
			NodeList nodeList = doc.getElementsByTagName(class1.getSimpleName());

			
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			String primaryKeyValue= elem.getAttribute(metamodel.getPrimaryKey().getName());
			System.out.println(">>>>>>>>"+primaryKeyValue.toString()+"<<<<<<<<>>>>>>"+primaryKey);
			if(primaryKeyValue.trim().equals(primaryKey.toString()))
			{
				
				return buildInstance(class1, elem, metamodel);
			}
			}
		}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			System.out.println("Object not found, Try changing search parameter");

		
		
		return null;
	}

	private T buildInstance(Class<T> clss, Element resultSet, XMLMetamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		T t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		if(primaryKeyType==long.class)
		{
			long primaryKey=Long.valueOf(resultSet.getAttribute(primaryKeyFieldName));
			primaryKeyField.setAccessible(true);
			primaryKeyField.set(t, primaryKey);
			
		}
		System.out.println(metamodel.getColumns());
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(resultSet.getElementsByTagName(columnName).item(0) == null)
				{
					field.set(t, null);
				}
				else
				{
					if(columnType==int.class)
					{
						int value=Integer.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==String.class)
					{
						String value=String.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==long.class)
					{
						Long value=Long.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}
					else if(columnType==double.class)
					{
						Double value=Double.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}
					else if(columnType.getSuperclass()== Object.class)
					{
						//String filePath = path+(resultSet.getElementsByTagName(columnName).item(0).getTextContent())+".ser";
						Object value=buildInstanceGeneric(columnType,(Element) resultSet.getElementsByTagName(columnName).item(0));
						field.set(t, value);
					}
				}
			}
			updateInjectListFields(metamodel, t);
		return t;
	}
	
	
	
	private Object buildInstanceGeneric(Class<?> clss, Element resultSet) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		XMLMetamodel metamodel = XMLMetamodel.of(clss);
		Object t =clss.getConstructor().newInstance();
		// No primary key in case of nested object
//		Field primaryKeyField = metamodel.getPrimaryKey().getField();
//		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
//		Class<?> primaryKeyType=primaryKeyField.getType();
//		
//		if(primaryKeyType==long.class)
//		{
//			long primaryKey=Long.valueOf(resultSet.getAttribute(primaryKeyFieldName));
//			primaryKeyField.setAccessible(true);
//			primaryKeyField.set(t, primaryKey);
//			
//		}
		System.out.println(metamodel.getColumns());
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(resultSet.getElementsByTagName(columnName).item(0) == null)
				{
					field.set(t, null);
				}
				else
				{
					if(columnType==int.class)
					{
						int value=Integer.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==String.class)
					{
						String value=String.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==long.class)
					{
						Long value=Long.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}
					else if(columnType.getSuperclass()== Object.class)
					{
						//String filePath = path+(resultSet.getElementsByTagName(columnName).item(0).getTextContent())+".ser";
						Object value=buildInstanceGeneric(columnType,(Element) resultSet.getElementsByTagName(columnName).item(0));
						field.set(t, value);
					}
				}
			}
			updateInjectListFields(metamodel, t);
		return t;
	}
	
	
	
	private T buildInstance_temp(Class<T> clss, Element resultSet, XMLMetamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		T t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		if(primaryKeyType==long.class)
		{
			long primaryKey=Long.valueOf(resultSet.getAttribute(primaryKeyFieldName));
			primaryKeyField.setAccessible(true);
			primaryKeyField.set(t, primaryKey);
			
		}
		System.out.println(metamodel.getColumns());
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(resultSet.getElementsByTagName(columnName).item(0) == null)
				{
					field.set(t, null);
				}
				else
				{
					if(columnType==int.class)
					{
						int value=Integer.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==String.class)
					{
						String value=String.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==long.class)
					{
						Long value=Long.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}
					else if(columnType.getSuperclass()== Object.class)
					{
						String filePath = path+(resultSet.getElementsByTagName(columnName).item(0).getTextContent())+".ser";
						Object value=readObjectFromFile(filePath);
						field.set(t, value);
					}
				}
			}
			updateInjectListFields(metamodel, t);
		return t;
	}
	
	

	
	private Object buildInstance(Class<?> clss, Element resultSet) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		XMLMetamodel metamodel = XMLMetamodel.of(clss);
		Object t =clss.getConstructor().newInstance();
		Field primaryKeyField = metamodel.getPrimaryKey().getField();
		String primaryKeyFieldName=metamodel.getPrimaryKey().getName();
		Class<?> primaryKeyType=primaryKeyField.getType();
		
		if(primaryKeyType==long.class)
		{
			long primaryKey=Long.valueOf(resultSet.getAttribute(primaryKeyFieldName));
			primaryKeyField.setAccessible(true);
			primaryKeyField.set(t, primaryKey);
			
		}
		System.out.println(metamodel.getColumns());
			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> columnType = columnField.getType();
				String columnName = columnField.getName();
				if(resultSet.getElementsByTagName(columnName).item(0)==null)
				{
					field.set(t, null);
				}
				else
				{
					if(columnType==int.class)
					{
						int value=Integer.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==String.class)
					{
						String value=String.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}else if(columnType==long.class)
					{
						Long value=Long.valueOf(resultSet.getElementsByTagName(columnName).item(0).getTextContent());
						field.set(t, value);
					}
					else if(columnType.getSuperclass()== Object.class)
					{
						Object value=buildInstanceGeneric(columnType,(Element) resultSet.getElementsByTagName(columnName).item(0));
						field.set(t, value);
					}

				}
			}
			updateInjectListFields(metamodel, t);
		return t;
	}
	
	
	
	
	
	
	private T updateObjectInXML_temp(T t, Element resultSet, XMLMetamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		//T t =clss.getConstructor().newInstance();
		PrimaryKeyField primaryKey = metamodel.getPrimaryKey();
		Field primaryKeyField = primaryKey.getField();
		primaryKeyField.setAccessible(true);
		String primaryKeyValue = primaryKeyField.get(t).toString();
		

			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> cfType = columnField.getType();
				String columnName = columnField.getName();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					File dir_class= new File(path+t.getClass().getName());
					if(!dir_class.exists())
					{
					
					dir_class.mkdir();
					}
					
					saveToFile(t, primaryKeyValue, columnField, path);
					//cfElem=getElements(doc,  columnField.getName(), t.getClass().getName()+File.separator+cf.getName()+"_"+primaryKeyValue);
					String value= t.getClass().getName()+File.separator+columnField.getName()+"_"+primaryKeyValue;
					resultSet.getElementsByTagName(columnName).item(0).setTextContent(value);
				}
				else
				{
				String value= field.get(t).toString();
				resultSet.getElementsByTagName(columnName).item(0).setTextContent(value);
				}
			}
		
		return t;
	}
	
	private T updateObjectInXML(T t, Element resultSet, XMLMetamodel metamodel) throws Exception {
		// TODO Auto-generated method stub
		//Metamodel metamodel=Metamodel.of(clss);
		//T t =clss.getConstructor().newInstance();
		PrimaryKeyField primaryKey = metamodel.getPrimaryKey();
		Field primaryKeyField = primaryKey.getField();
		primaryKeyField.setAccessible(true);
		String primaryKeyValue = primaryKeyField.get(t).toString();
		
		

			for (ColumnField columnField : metamodel.getColumns()) {
				Field field = columnField.getField();
				field.setAccessible(true);
				Class<?> cfType = columnField.getType();
				String columnName = columnField.getName();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					
					Element childElement = (Element)resultSet.getElementsByTagName(columnName).item(0);
					Object obj = field.get(t);
					
					updateTNodeGeneric(obj, childElement);
					

				}
				else
				{
				String value= field.get(t).toString();
				resultSet.getElementsByTagName(columnName).item(0).setTextContent(value);
				}
			}
		
		return t;
	}
	
	
	
	private Object updateTNodeGeneric( Object t, Element element) {

//		PrimaryKeyField primaryField =metamodel.getPrimaryKey();
		XMLMetamodel metamodel=XMLMetamodel.of(t.getClass());
//		primaryField.getField().setAccessible(true);
		try {
//			String primaryKeyValue = primaryField.getField().get(t).toString();
//			tNode.setAttribute(metamodel.getPrimaryKey().getName(), primaryField.getField().get(t).toString());
			
			for(ColumnField cf:metamodel.getColumns())
			{
				try
				{
				cf.getField().setAccessible(true);
				Node cfElem =null;
				Class<?> cfType = cf.getField().getType();
				if(cfType!=String.class && cfType!=Long.class && cfType!=Integer.class && cfType.getSuperclass()==Object.class)
				{
					Element childElement = (Element)element.getElementsByTagName(cf.getName()).item(0);
					Object obj = cf.getField().get(t);
					updateTNodeGeneric(obj, childElement);
					
				}
					else	
				{
						String value= cf.getField().get(t).toString();
						element.getElementsByTagName(cf.getName()).item(0).setTextContent(value);
				}
				}catch(NullPointerException e)
				{
					System.out.println("the value for column Field <"+cf.getName()+"> is null, Please provide default value in class to avoid this error.");
				}
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return t;
	}
	

	@Override
	public List find(Class<T> class1, String columnName, Object columnValue) throws Exception {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(class1);
		List list = new ArrayList();
		
		File file = new File(metamodel.getXMLFilePath(path));
		if(!file.exists())
		{
			return null;
		}
		try {
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();  
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
			NodeList nodeList = doc.getElementsByTagName(class1.getSimpleName());

			
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			//elem.remo
			String elemColumnValue=elem.getElementsByTagName(columnName).item(0).getTextContent();
			//String primaryKeyValue= elem.getAttribute(metamodel.getPrimaryKey().getName());
			//System.out.println(">>>>>>>>"+elemColumnValue+"<<<<<<<<>>>>>>"+columnValue);
			if(elemColumnValue.trim().toLowerCase().equals(columnValue.toString().toLowerCase()))
			{
				
				list.add( buildInstance(class1, elem, metamodel));
			}
			
			}
		return list;
		}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			System.out.println("Object not found, Try changing search parameter");

		
		
		return null;
	}
	
	
	@Override
	public List<T> find(Class<T> class1, Predicate<T> predicate) throws Exception {
		XMLMetamodel metamodelT = XMLMetamodel.of(class1);
		String sql=metamodelT.buildSelectRequestAllRows();
		List<T> allTElements = (List<T>)buildInstance(class1, metamodelT);
		
		List<T> list=allTElements
				.stream()
				.filter((t)->predicate.test(t))
				.map((t)->t)
				.collect(Collectors.toList());

		return list;
	}
	
	
	@Override
	public List find(Class<?> class1, Object columnValue, String columnName) throws Exception {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(class1);
		List list = new ArrayList();
		
		File file = new File(metamodel.getXMLFilePath(path));
		if(!file.exists())
		{
			return null;
		}
		try {
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();  
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
			NodeList nodeList = doc.getElementsByTagName(class1.getSimpleName());

			
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			//elem.remo
			String elemColumnValue="";
			if(elem.getElementsByTagName(columnName).item(0)!=null)
			{
				elemColumnValue = elem.getElementsByTagName(columnName).item(0).getTextContent();
			}
			else if(elem.getAttribute(columnName)!=null)
			{
				elemColumnValue=elem.getAttribute(columnName).toString();
			}
			else
			{
				elemColumnValue="";	
			}
			//String primaryKeyValue= elem.getAttribute(metamodel.getPrimaryKey().getName());
			//System.out.println(">>>>>>>>"+elemColumnValue+"<<<<<<<<>>>>>>"+columnValue);
			if(elemColumnValue.trim().toLowerCase().equals(columnValue.toString().toLowerCase()))
			{
				
				list.add( buildInstance(class1, elem));
			}
			
			}
		return list;
		}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			System.out.println("Object not found, Try changing search parameter");

		
		
		return null;
	}
	
	

	@Override
	public void update(T t, String columnName) throws SQLException, IllegalArgumentException, IllegalAccessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public T delete(Class<T> class1, Object primaryKey) throws Exception {
		// TODO Auto-generated method stub
		XMLMetamodel metamodel=XMLMetamodel.of(class1);
		
		File file = new File(metamodel.getXMLFilePath(path));
		try {
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();  
			Element root=doc.getDocumentElement();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
			NodeList nodeList = doc.getElementsByTagName(class1.getSimpleName());

			
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			String primaryKeyValue= elem.getAttribute(metamodel.getPrimaryKey().getName());
			System.out.println(">>>>>>>>"+primaryKeyValue.toString()+"<<<<<<<<>>>>>>"+primaryKey);
			if(primaryKeyValue.trim().equals(primaryKey.toString()))
			{
				
				root.removeChild(elem);
				savetoFile(metamodel, doc);
				return buildInstance(class1, elem, metamodel);
			}
			
			
			
			}
		}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			System.out.println("Object not found, Try changing search parameter");

		
		
		return null;
	}

	@Override
	public void createTable(Class<T> clss)
	{
		System.out.println(" This feature not supported for XMLEntities");
	}
	public void createFile(Class<T> clss) {
		// TODO Auto-generated method stub

        XMLMetamodel metamodel=XMLMetamodel.of(clss);
        if(new File(metamodel.getXMLFilePath(path)).exists())
        {
        	System.out.println("File Already exists, Please save it and remove to avoid an loss of data");
        	return;
        }

		try {
	        
	        Document doc = dBuilder.newDocument();
            Element rootElement =
                    doc.createElementNS("http://www.w3.org", "Class");
            doc.appendChild(rootElement);
//			Object ob = clss.getConstructor().newInstance();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            //for pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult console = new StreamResult(System.out);
            StreamResult file = new StreamResult(new File(metamodel.getXMLFilePath(path)));
            transformer.transform(source, console);
            transformer.transform(source, file);
            System.out.println("DONE");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	Map<Class<?>,Supplier> registry = new HashMap<>();
	public <U>List<U> join(Object t, Class<U> classU) {
	return super.join(t, classU);
	
	}
//	@Override
//	public List join(Object t, Class<?> classU) {
//		List list=null;
//		
//		
//	Metamodel metamodelT = Metamodel.of(t.getClass());
//	Metamodel metamodelU = Metamodel.of(classU);
//	System.out.println("Class for U::::::::"+classU.getClass());
//	
//	List <Class<?>> classes = List.of(t.getClass());
//	for (Class<?> clss : classes) {
//		Method[] methods= clss.getDeclaredMethods();
//		for (Method method : methods) {
//			Provides provides = method.getAnnotation(Provides.class);
//	
//			if(provides !=null)
//			{
//				//System.out.println("provides here");
//				Class<?> returnType=method.getReturnType();
//
//				
//				Supplier<?> supplier = () -> {
//					try {
//						if (!Modifier.isStatic(method.getModifiers())) {
//							
//							return method.invoke(t);
//						} else {
//							return method.invoke(null);
//						}
//					} catch (Exception e) {
//						throw new RuntimeException(e);
//					}
//				};
//				
//				registry.put(returnType, supplier);
//			}
//		}
//		
//		
//
//		//valueSetter=0;
//		//metamodelU.getColumns().stream().filter((f)->f.getField().getAnnotation(Inject.class)!=null).forEach((f)->setValueSetter(valueSetter+1));
//		List<ColumnField> temp = metamodelU.getAnnotationMap().get(Inject.class);
//		if(temp == null);
//		
//		int j=temp.size();
//		ColumnField[] injectArray= new ColumnField[j];
//		List<ColumnField> injectList=temp.stream()
//							.filter((f)->f.getField().getAnnotation(Inject.class).classname().equals(clss.getName())).collect(Collectors.toList());
//		int n=injectList.size();
//		
//
//		Object[] valueList=new Object[n];
//		ColumnField[] fieldist=new ColumnField[n];
//		int i=0;
//		for(ColumnField field:injectList)
//		{Object value=null;
//			try {
//
//				Class<?> returnType = field.getField().getType();
//				//System.out.println("field Return Type:::: "+returnType);
//				value = registry.get(returnType).get();
//				//System.out.println("Query Key:---------:-"+registry.get(returnType).get());
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//				try {
//				//Object uObject =  classU.getConstructor().newInstance();
//				//field.getField().setAccessible(true);
//				//field.getField().set(uObject, value);
//				fieldist[i]=field;
//				valueList[i]=value;
//				
//				//list= findGeneric(classU, field,value);
//				
//				}
//				catch(Exception e)
//				{
//					e.printStackTrace();
//				}
//				i++;
//			
//		}
//		try {
//			list=findGenericMultipleConditions(classU, fieldist, valueList);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//	}
//	
//	
//	
//	
//		
//		
//		return list;
//		
//		
//	}
	
	
	@Override
	public List findGenericMultipleConditions(Class<?> clss, ColumnField[] queryKey, Object[] value) throws Exception{
		List<Object> list = new ArrayList<>();
		File file = new File(XMLMetamodel.of(clss).getXMLFilePath(path));
		System.out.println("file here>>>>"+file);
		
		Document doc = dBuilder.parse(file);
		doc.getDocumentElement().normalize();  
		System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
		NodeList nodeList = doc.getElementsByTagName(clss.getSimpleName());

		
		for (int itr = 0; itr < nodeList.getLength(); itr++)   
		{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			//String 
			List paramList= IntStream.range(0, queryKey.length)
			.filter((i)->elem.getElementsByTagName(queryKey[i].getName()).item(0).getTextContent().equals(value[i].toString()))
			.mapToObj(i->value[i])
			.collect(Collectors.toList());
			//System.out.println(queryKey[0]);
			if(paramList.size()==value.length)
			{
				list.add(buildInstance(clss, elem));
			}
			

		}
		return list;
	}
	
	
	
	
	public boolean ifExists(Class<T> clss, ColumnField[] queryKey, Object[] value, T t) throws Exception{
		List<Object> list = new ArrayList<>();
		XMLMetamodel metamodel = XMLMetamodel.of(clss);
		File file = new File(metamodel.getXMLFilePath(path));
		if(!file.exists())
		{
			return false;
		}
		Document doc = dBuilder.parse(file);
		doc.getDocumentElement().normalize();  
		System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
		NodeList nodeList = doc.getElementsByTagName(clss.getSimpleName());

		
		for (int itr = 0; itr < nodeList.getLength(); itr++)   
		{ 
			Node node=nodeList.item(itr);
			Element elem=(Element) node;
			//String 
			List paramList= IntStream.range(0, queryKey.length)
			.filter((i)->elem.getElementsByTagName(queryKey[i].getName()).item(0).getTextContent().equals(value[i].toString()))
			.mapToObj(i->value[i])
			.collect(Collectors.toList());
			//System.out.println(queryKey[0]);
			if(paramList.size()==value.length)
			{
				T cloneObject =buildInstance(clss, elem, metamodel);
				Field primaryKeyField = metamodel.getPrimaryKey().getField();
				primaryKeyField.setAccessible(true);
				primaryKeyField.set(t, primaryKeyField.get(cloneObject));
				return true;
			}
			

		}
		return false;
	}

//	@Override
//	public Map genericJoin(Class<?> classT, Class<?> classU) {
//		// TODO Auto-generated method stub
//		return null;
//	}


	@Override
	public void searchAndUpdateDependency(T t) {
		super.searchAndUpdateDependency(t);
		
	}

	
	private <T>EntityManager<T> wrap(Stack<List<Wrapper>> stack,String path, Map<Class<?>,List<Wrapper>> wrapperListToClassMap, DocumentBuilder dBuilder)
	{
		return new XMLEntityManager<T>(stack,path, wrapperListToClassMap,dBuilder);
	}
	
	protected <U>EntityManager<U> wrap(String path)
	{
		return new XMLEntityManager<U>(null,path,null, null);
	}
	
	private XMLEntityManager(Stack<List<Wrapper>> stack,String path, Map<Class<?>,List<Wrapper>> wrapperListToClassMap, DocumentBuilder dBuilder) {
		super();
		this.stack = stack;
		this.path=path;
		this.wrapperListToClassMap=wrapperListToClassMap;
		this.dBuilder=dBuilder;
	}
	
	@Override
	protected <U>EntityManager<U> classToObjGenericWrap(Class<U> clss)
	{
		
		return wrap(stack,path, wrapperListToClassMap,dBuilder);
	}

}
