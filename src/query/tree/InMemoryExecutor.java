package query.tree;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import reflection.utility.JoinType;

import java.util.Set;

import util.ColumnField;
import util.Metamodel;

public class InMemoryExecutor implements Executor {
	
	public static Executor getInMemoryExecutor() {
		return new InMemoryExecutor();
	}

    @Override
    public <T> List<T> execute(Query<T> query, List<T> data) {


        // Apply joins (if any)
        //data = applyJoins(data, query.getJoins());

        // Apply filters
        return data.stream()
                .filter(obj -> evaluate(query.getCondition(), obj))
                .collect(Collectors.toList());
    }
    
    
	@Override
	public <T> List<JoinedRow> executeWithJoin(Query<T> query, Map<Class<?>, List<?>> entityListDataMap) {
		// TODO Auto-generated method stub
		return generateRows(query, entityListDataMap);
	}
	
	
	private <T>List<JoinedRow> generateRows(Query<T> query, Map<Class<?>, List<?>> entityListDataMap){
		List<JoinedRow> joinedRows = new ArrayList<>();
		List<Join<?, ?>> joinList = query.getJoins();
		
		if(joinList == null || joinList.isEmpty()) {
			throw new RuntimeException("this method only for join functionality call execute for only condition based results.");
		}
		Map<Object, List<Object>> index =new HashMap<>();
		for(Join<?, ?> join : joinList) {
			
			indexData(index, entityListDataMap.get(join.getRightEntity()), join.getRightField());			
		}
		 
		
		for(Join<?, ?> join : joinList) {
			
			List<JoinedRow> resultRow = mapJoinedRow(index,entityListDataMap.get(join.getLeftEntity()), join);
			joinedRows.addAll(resultRow);
			
		}
		
		if(query.getCondition() != null) {
			joinedRows = joinedRows.stream()
								.filter((row)->evaluate(query.getCondition(), row))
								.collect(Collectors.toList());
		}
		
		return joinedRows;
	}
	
	
	private <T>List<JoinedRow> mapJoinedRow(Map<Object, List<Object>> index, List<?> data, Join<T, ?> join){
		
		List<JoinedRow> results = new ArrayList<>();		
		
		for (Object leftObj : data) {
		    Object leftKey =
		            getFieldValue(
		                leftObj,
		                join.getLeftField()
		            );
		        List<Object> matches =
		            index.get(leftKey);
		        if (matches != null) {
		            for (Object rightObj : matches) {
		                JoinedRow row =
		                    new JoinedRow();
		                row.put(
		                    leftObj.getClass(),
		                    leftObj
		                );
		                row.put(
		                    rightObj.getClass(),
		                    rightObj
		                );
		                results.add(row);
		            }
		        }else if(matches == null && join.getType() == JoinType.LEFT){
		        	JoinedRow row =
		                    new JoinedRow();
		        	row.put(leftObj.getClass(),
		                    leftObj
		        			);
		        	results.add(row);
		        }
		    }
		
		return results;
	}
	
	
	private void indexData(Map<Object, List<Object>> index, List<?> data, String fieldName) {
		
		
		for (Object rightObj : data) {
		    Object key =
		        getFieldValue(
		            rightObj,
		            fieldName
		        );
		    index.computeIfAbsent(
		        key,
		        k -> new ArrayList<>()
		    ).add(rightObj);
		}
		
		
	}

//    private <T>List<T> applyJoins(List<T> data, List<Join<T, ?>> joins) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	private <T>List<T> loadData(Class<T> entityClass) {
//		
//		return null;
//	}
	
	public <T> Predicate<T> compile(Condition<?> condition) {
	    switch (condition.getOperator()) {

	        case EQ:
	            return obj -> {
	                Object fieldValue = getFieldValue(obj, condition.getField(), condition.getValue());
	                return fieldValue == null 
	                    ? condition.getValue() == null 
	                    : fieldValue.equals(condition.getValue());
	            };

	        case GT:
	            return obj -> {
	                Comparable f = (Comparable) getFieldValue(obj, condition.getField(), condition.getValue());
	                Comparable v = (Comparable) condition.getValue();
	                return f.compareTo(v) > 0;
	            };

	        case AND:
	            List<Predicate<T>> andPredicates =condition.getChildren()
		                .stream()
		                .map(this::<T>compile)
		                .collect(Collectors.toList());

	            return obj -> {
	                for (Predicate<T> p : andPredicates) {
	                    if (!p.test(obj)) return false;
	                }
	                return true;
	            };

	        case OR:
	            List<Predicate<T>> orPredicates = condition.getChildren()
	                .stream()
	                .map(this::<T>compile)
	                .collect(Collectors.toList());

	            return obj -> {
	                for (Predicate<T> p : orPredicates) {
	                    if (p.test(obj)) return true;
	                }
	                return false;
	            };

	        default:
	            throw new UnsupportedOperationException();
	    }
	}

	private <T>Comparable getFieldValue(T obj, String field, Object conditionValue) {
		// TODO Auto-generated method stub
		Class<T> clss = (Class<T>) obj.getClass();
		Metamodel metamodel = Metamodel.of(clss);
		Map<String, ColumnField> columnFields = metamodel.getNameFieldMapping();
		Field fld = columnFields.get(field)!=null? columnFields.get(field).getField(): null;
		if(fld == null) {
			if(metamodel.getPrimaryKey()!=null && field.equals(metamodel.getPrimaryKey().getName())) {			
				fld = metamodel.getPrimaryKey().getField();
			}else {
				return null;
			}
		}
		
		fld.setAccessible(true);
		Comparable value = null;
		try {
			value = (Comparable)fld.get(obj);
			if (!value.getClass().isAssignableFrom(conditionValue.getClass())) {
			    throw new RuntimeException("Type mismatch: " + value.getClass() + " vs " + conditionValue.getClass());
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	private <T>Comparable getFieldValue(T obj, String field) {
		// TODO Auto-generated method stub
		Class<T> clss = (Class<T>) obj.getClass();
		Metamodel metamodel = Metamodel.of(clss);
		Map<String, ColumnField> columnFields = metamodel.getNameFieldMapping();
		Field fld = columnFields.get(field)!=null? columnFields.get(field).getField(): null;
		if(fld == null) {
			if(metamodel.getPrimaryKey()!=null && field.equals(metamodel.getPrimaryKey().getName())) {			
				fld = metamodel.getPrimaryKey().getField();
			}else {
				return null;
			}
		}
		
		fld.setAccessible(true);
		Comparable value = null;
		try {
			value = (Comparable)fld.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	private <T> boolean evaluate(Condition<?> condition, T obj) {
        // Recursive evaluation of condition tree
		Operator operator = condition.getOperator();
		Object value = condition.getValue();
		String fieldName = condition.getField(); 
		Class<T> clss = (Class<T>) obj.getClass();
		Metamodel metamodel = Metamodel.of(clss);
		Map<String, ColumnField> columnFields = metamodel.getNameFieldMapping();
		Field fld = columnFields.get(fieldName)!=null? columnFields.get(fieldName).getField(): null;
		boolean returnValue = false;
		boolean compositeOperator = false;
		Object fieldValue = null;
		if(fld == null && !(condition.getOperator() == Operator.AND || condition.getOperator() == Operator.OR)) {
			return false;
		}else{
			if(fld == null) {
				compositeOperator = true;
			}
			
		}
		if(!compositeOperator) {
			
			fld.setAccessible(true);
			
			
			try {
				fieldValue = fld.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException("Error evaluating condition", e1);
			}

			if (fieldValue == null) {
				if(operator == Operator.EN) {
					return true;
				}
			    return operator == Operator.EQ ? value == null : operator == Operator.NE && value != null;
			}
			
			try {
				Comparable comparableFieldValue = (Comparable) fld.get(obj);
				if ((!(fieldValue instanceof Comparable)
					    || !(value instanceof Comparable)) && operator !=Operator.EN && operator !=Operator.NN) {
				    throw new RuntimeException("Type mismatch: " + fieldValue.getClass() + " vs " + value.getClass());
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException("Error evaluating condition", e1);
			}
			
			
		}

		
		
		try {
				switch(operator) {
				
				case EQ: 
					returnValue = fieldValue.equals(value);
					break;
				case GT: 
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) > 0;
					break;
				case GTE:
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) >= 0;
					break;
				case LT:
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) < 0;
					break;
				case NE:
					returnValue = !fieldValue.equals(value);
					break;
				case EN:
					returnValue = fieldValue == null;
					break;
				case NN:
					returnValue = fieldValue != null;
					break;
				case LTE:
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) <= 0;
					break;
				case IN:
				    Collection<?> values = (Collection<?>) condition.getValues();

				    returnValue = values.contains(fieldValue);
				case LIKE:

				    String pattern = condition.getValue().toString()
				        .replace("%", ".*");

				    returnValue = fieldValue.toString().matches(pattern);
				case BETWEEN:

				    List<?> range = (List<?>) condition.getValues();

				    Comparable start = (Comparable) range.get(0);
				    Comparable end = (Comparable) range.get(1);

				    Comparable actual = (Comparable) fieldValue;

				    returnValue = actual.compareTo(start) >= 0
				        && actual.compareTo(end) <= 0;
				case AND:
					returnValue = true;
					for (Condition<?> child : condition.getChildren()) {
					    if (!evaluate(child, obj)) {
					    	returnValue = false;
					    	break; // break for optimization if the current condition false then no need to check further
					    	
					    }
					}
					break;
					
				case OR:
					returnValue = false;
					for (Condition<?> child : condition.getChildren()) {
					    if (evaluate(child, obj)) {
					    	returnValue = true;
					    	break; // break for optimization if the current condition true then no need to check further
					    	
					    } 
					}						
					break;
					
				default:
				    throw new UnsupportedOperationException("Unsupported operator: " + operator);
				
				}
		
		
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error evaluating condition", e);
		}
		
		
		
        return returnValue;
    }
	
	
	
	private boolean evaluate(Condition<?> condition, JoinedRow row) {
        // Recursive evaluation of condition tree
		Operator operator = condition.getOperator();
		Class<?> clss = condition.getEntityClass();
		Object value = condition.getValue();
		String fieldName = condition.getField(); 
		
		Object obj = row.get(clss);
		if(obj == null) {
			return false; // if condition object does not present in the joined row then we need to return false to imitate
			              // sql join behavior with where conditions
		}
		Metamodel metamodel = Metamodel.of(clss);
		Map<String, ColumnField> columnFields = metamodel.getNameFieldMapping();
		Field fld = columnFields.get(fieldName)!=null? columnFields.get(fieldName).getField(): null;
		boolean returnValue = false;
		boolean compositeOperator = false;
		Object fieldValue = null;
		if(fld == null && !(condition.getOperator() == Operator.AND || condition.getOperator() == Operator.OR)) {
			return false;
		}else{
			if(fld == null) {
				compositeOperator = true;
			}
			
		}
		if(!compositeOperator) {
			
			fld.setAccessible(true);
			
			
			try {
				fieldValue = fld.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException("Error evaluating condition", e1);
			}

			if (fieldValue == null) {
				if(operator == Operator.EN) {
					return true;
				}
			    return operator == Operator.EQ ? value == null : operator == Operator.NE && value != null;
			}
			
			try {
				Comparable comparableFieldValue = (Comparable) fieldValue;
				if ((!(fieldValue instanceof Comparable)
					    || !(value instanceof Comparable)) && operator !=Operator.EN && operator !=Operator.NN) {
				    throw new RuntimeException("Type mismatch: " + fieldValue.getClass() + " vs " + value.getClass());
				}
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException("Error evaluating condition", e1);
			}
			
			
		}

		
		
		try {
				switch(operator) {
				
				case EQ: 
					returnValue = fieldValue.equals(value);
					break;
				case GT: 
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) > 0;
					break;
				case GTE:
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) >= 0;
					break;
				case LT:
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) < 0;
					break;
				case NE:
					returnValue = !fieldValue.equals(value);
					break;
				case EN:
					returnValue = fieldValue == null;
					break;
				case NN:
					returnValue = fieldValue != null;
					break;
				case LTE:
					returnValue = ((Comparable)fieldValue).compareTo((Comparable)value) <= 0;
					break;
				case IN:
				    Collection<?> values = (Collection<?>) condition.getValues();

				    returnValue = values.contains(fieldValue);
				    break;
				case LIKE:

				    String pattern = condition.getValue().toString()
				        .replace("%", ".*");

				    returnValue = fieldValue.toString().matches(pattern);
				    break;
				case BETWEEN:

				    List<?> range = (List<?>) condition.getValues();

				    Comparable start = (Comparable) range.get(0);
				    Comparable end = (Comparable) range.get(1);

				    Comparable actual = (Comparable) fieldValue;

				    returnValue = actual.compareTo(start) >= 0
				        && actual.compareTo(end) <= 0;
				    break;
				case AND:
					returnValue = true;
					for (Condition<?> child : condition.getChildren()) {
					    if (!evaluate(child, row)) {
					    	returnValue = false;
					    	break; // break for optimization if the current condition false then no need to check further
					    	
					    }
					}
					break;
					
				case OR:
					returnValue = false;
					for (Condition<?> child : condition.getChildren()) {
					    if (evaluate(child, row)) {
					    	returnValue = true;
					    	break; // break for optimization if the current condition true then no need to check further
					    	
					    } 
					}						
					break;
					
				default:
				    throw new UnsupportedOperationException("Unsupported operator: " + operator);
				
				}
		
		
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error evaluating condition", e);
		}
		
		
		
        return returnValue;
    }


	
}