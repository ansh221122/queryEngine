package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.simple.JSONObject;

import reflection.utility.Column;
import reflection.utility.PrimaryKey;
import reflection.utility.injectlist;
import reflection.utility.relation;

public class JsonMetamodel implements MetamodelStructure{

	
	protected Class<?> clss;
	protected static Map<Class,JsonMetamodel> classMap =new HashMap();
protected JsonMetamodel(Class<?> clss) {
		// TODO Auto-generated constructor stub
	//System.out.println("class####"+clss);
this.clss=clss;

}

public static JsonMetamodel of(Class<?> clss)
{
	if(classMap.get(clss)==null)
	{
		classMap.put(clss, new JsonMetamodel(clss));
	}
	//System.out.println("metamodel object----->"+classMap.get(clss));
	
	return classMap.get(clss);

}




public JSONObject getNewJsonObject()
{

	JSONObject obj = new JSONObject();
	obj.put(getPrimaryKey().getName(), null);
	setFieldsInJsonObject(obj);
	
	return obj;
}

public JSONObject getNewJsonObjectWithoutPrimaryKey()
{

	JSONObject obj = new JSONObject();
	setFieldsInJsonObject(obj);
	
	return obj;
}



public void setFieldsInJsonObject(JSONObject obj) {
	List<ColumnField> list = getColumns();
	//create template object for entity object
	for(ColumnField cf : list)
	{
		obj.put(cf.getName(), null);
	}
}

public String getJsonFilePath(String path)
{

return path+clss.getSimpleName()+".json";	
}

public PrimaryKeyField getPrimaryKey() {
	// TODO Auto-generated method stub
Field fields[]=clss.getDeclaredFields();
for(Field fld:fields)
{
	PrimaryKey primaryKey=fld.getAnnotation(PrimaryKey.class);
	if(primaryKey!=null)
	{
		PrimaryKeyField primaryKeyField = new PrimaryKeyField(fld);
		return primaryKeyField;
	}
}
	
	return null;
}

public List<Field> getInjectList() {
	// TODO Auto-generated method stub
	List<Field> al=new ArrayList<>();
	Field fields[]=clss.getDeclaredFields();
	for(Field fld:fields)
	{
		relation columnKey=fld.getAnnotation(relation.class);
		if(columnKey!=null && fld.getType().getSuperclass()== List.class)
		{
			
			al.add(fld);
		}
	}
	return al;
}

public List<ColumnField> getColumns() {
	// TODO Auto-generated method stub
	List<ColumnField> al=new ArrayList<>();
	Field fields[]=clss.getDeclaredFields();
	for(Field fld:fields)
	{
		Column columnKey=fld.getAnnotation(Column.class);
		if(columnKey!=null)
		{
			ColumnField columnKeyField = new ColumnField(fld);
			al.add(columnKeyField);
		}
	}
	return al;
}





public Map<Class<?>,List<ColumnField>> getAnnotationMap() {
	// TODO Auto-generated method stub
	HashMap<Class<?>,List<ColumnField>> annotationMap = new HashMap();
	List<ColumnField> al=getColumns();
	for(ColumnField fld:al)
	{
		
		Annotation[] arr=fld.getField().getAnnotations(); // one field can have multiple annotation associated with it
		for(Annotation an:arr)
		{
			List<ColumnField> listColumnField;
			if(annotationMap.get(an.annotationType())==null)
			{
				listColumnField=new ArrayList();
			}
			else
			{
				listColumnField=annotationMap.get(an.annotationType());
			}
			listColumnField.add(fld);
			annotationMap.put(an.annotationType(),listColumnField);
		}
	}
	return annotationMap;
}



public Map<String ,ColumnField> getNameFieldMapping() {
	// TODO Auto-generated method stub
	HashMap<String,ColumnField> fieldMap = new HashMap();
	List<ColumnField> al=getColumns();
	for(ColumnField fld:al)
	{
		
		fieldMap.put(fld.getName(), fld);

		
	}
	return fieldMap;
}

public String buildInsertQuery() {
	// TODO Auto-generated method stub
	String columnElement = buildColumnNames();
	String questionMarksElement = buildQuestionMarksElement();
	
	return "INSERT INTO "+this.clss.getSimpleName()+"("+columnElement+") values("+questionMarksElement+")";
}
public String buildSelectRequest() {
	// TODO Auto-generated method stub
    // select id, name, age from Person;
	String columnElement = buildColumnNames();
	return "select "+columnElement+" from "+this.clss.getSimpleName()+" "
			+ " where "+getPrimaryKey().getName()+" = ?";
}


public String buildSelectRequest(ColumnField field ) {
	// TODO Auto-generated method stub
    // select id, name, age from Person;
	String columnElement = buildColumnNames();
	return "select "+columnElement+" from "+this.clss.getSimpleName()+" "
			+ " where "+field.getName()+" = ?";
}

public String buildSelectRequestAllRows() {
	// TODO Auto-generated method stub
    // select id, name, age from Person;
	String columnElement = buildColumnNames();
	return "select "+columnElement+" from "+this.clss.getSimpleName()+" ";
}

public String buildSelectRequest(ColumnField[] field ) {
	// TODO Auto-generated method stub
    // select id, name, age from Person;
	String columnElement = buildColumnNames();
	System.out.println("Field 0 ::::"+field[0].getName());
	List<String> fieldList= Arrays.stream(field).filter((f)->f!=null)
			.map((f)->" "+f.getName()+"=? ")
			.collect(Collectors.toList());
	//System.out.println(String.join(" AND ", fieldList));
	return "select "+columnElement+" from "+this.clss.getSimpleName()+" "
			+ " where "+String.join(" AND ", fieldList);
}

private String buildQuestionMarksElement() {
	int numberOfColumns=getColumns().size()+1;
	String 	questionMarksElement= IntStream.range(0, numberOfColumns).mapToObj(Index -> "?").collect(Collectors.joining(","));
	return questionMarksElement;
}

private String buildColumnNames() {
	String primaryKeyColumnName=getPrimaryKey().getName();
	List<String> columnNames=getColumns().stream().map(ColumnField::getName).collect(Collectors.toList());
	System.out.println("Column names:::"+columnNames);
	columnNames.add(0,primaryKeyColumnName);
	String columnElement=String.join(",", columnNames);
	return columnElement;
}

private String buildUpdateColumnNames() {
	//String primaryKeyColumnName=getPrimaryKey().getName()+;
	List<String> columnNames=getColumns().stream().map((f)->f.getName()+"="+"?").collect(Collectors.toList());
	//columnNames.add(0,primaryKeyColumnName);
	String columnElement=String.join(",", columnNames);
	return columnElement;
}

public String buildColumnNamesCreate() {
	System.out.println("primary key type::"+getPrimaryKey().getType());
	String primaryKeyColumnName=getPrimaryKey().getName()+" "+(getPrimaryKey().getType()==long.class?"int(11) PRIMARY KEY":"text");
	List<String> columnNames=getColumns().stream().map((f)->f.getName()+" "+(f.getType()==Integer.class?"int(11)":"text")).collect(Collectors.toList());
	columnNames.add(0,primaryKeyColumnName);
	String columnElement=String.join(",", columnNames);
	return columnElement;
}

public String checkForPrimaryKeyFieldMaxValueQuery() {
	// TODO Auto-generated method stub
	//String primaryKeyColumnName=;
	String sql="SELECT MAX("+getPrimaryKey().getName()+") AS "+getPrimaryKey().getName()+" FROM "+clss.getSimpleName();
	
	return sql;
}

public String buildUpdateQuery() {
	// TODO Auto-generated method stub
	
	String columnElement = buildUpdateColumnNames();
	String sql="UPDATE "+clss.getSimpleName()+" SET "+columnElement+" WHERE "+getPrimaryKey().getName()+"= ?";
	System.out.println("update query::::"+sql);
	//String questionMarksElement = buildQuestionMarksElement();
	return sql;
}


public String buildUpdateQueryGenericColumnFilter(String columnName) {
	// TODO Auto-generated method stub
	
	String columnElement = buildUpdateColumnNames();
	String sql="UPDATE "+clss.getSimpleName()+" SET "+columnElement+" WHERE "+columnName+"= ?";
	System.out.println("update query::::"+sql);
	//String questionMarksElement = buildQuestionMarksElement();
	return sql;
}

public String buildCreateQuery()
{
	String sql="CREATE TABLE "+clss.getSimpleName()+"("+buildColumnNamesCreate()+")";
return sql;
	
}

public String buildDeletionRequest() {
	// TODO Auto-generated method stub
	
	return "delete"+" from "+this.clss.getSimpleName()+" "
	+ " where "+getPrimaryKey().getName()+" = ?";
	
}

public String buildSelectRequestGenericColumnFilter(String columnName) {
	// TODO Auto-generated method stub
    // select id, name, age from Person;
	String columnElement = buildColumnNames();
	return "select "+columnElement+" from "+this.clss.getSimpleName()+" "
			+ " where "+columnName+" = ?";
}


}
