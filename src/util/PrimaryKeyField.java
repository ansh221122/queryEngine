package util;

import java.lang.reflect.Field;

import reflection.utility.PrimaryKey;

public class PrimaryKeyField {

	private Field field;
	private PrimaryKey primaryKey;
	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public PrimaryKeyField(Field fld) {
		// TODO Auto-generated constructor stub
	this.field=fld;
	this.primaryKey=field.getAnnotation(PrimaryKey.class);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return primaryKey.name();
	}

	public Class<?> getType() {
		// TODO Auto-generated method stub
		return field.getType();
	}



}
