package util;

import java.lang.reflect.Field;

import reflection.utility.Column;

public class ColumnField {

	private Field field;
	private Column column;
	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
		this.column=this.field.getAnnotation(Column.class);
	}

	public ColumnField(Field fld) {
		// TODO Auto-generated constructor stub
	this.field=fld;
	this.column=this.field.getAnnotation(Column.class);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return column.name();
	}

	public Class<?> getType() {
		// TODO Auto-generated method stub
		return field.getType();
	}

	public ColumnField() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
