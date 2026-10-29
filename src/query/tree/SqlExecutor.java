package query.tree;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import reflection.utility.EntityManager;
import util.ColumnField;
import util.Metamodel;

public class SqlExecutor implements Executor {
	
	private Metamodel metamodel = null; 
	
	private EntityManager entityManager = null;
	
	public <T>SqlExecutor(Metamodel metamodel, EntityManager<T> entityManager) {
		this.metamodel = metamodel;
		this.entityManager = entityManager;
		
	}

    @Override
    public <T> List<T> execute(Query<T> query, List<T> data) {

        QueryFragment queryFragment = buildSql(query);

        // Execute via JDBC / ORM
        // map ResultSet → objects

        return entityManager.buildWithQuery(queryFragment.getSql(), queryFragment.getParams());
    }

    
    private <T>QueryFragment buildSql(Query<T> query) {
        // Convert Condition → WHERE clause
        // Convert Join → SQL JOIN
    	String generatedSql = "";
    	String selectQuery = metamodel.buildSelectRequestAllRows();
    	
    	QueryFragment whereQueryFragment = getQueryConditions(query.getCondition());
    	generatedSql = selectQuery + " WHERE " + whereQueryFragment.getSql() ;
    	System.out.println("Generated SQL:"+generatedSql);
    	List<Object> params =  whereQueryFragment.getParams()
    							.stream()
    							.filter((param)->param!=null)
    							.collect(Collectors.toList());
        return new QueryFragment(generatedSql, params);
    }
    
    
    private <T>QueryPlan buildSqlWithJoin(Query<T> query) {
        // Convert Condition → WHERE clause
        // Convert Join → SQL JOIN
    	String generatedSql = "";
    	List<SelectFieldMeta> selectFieldList = new ArrayList<>();
    	
    	HashSet<Class<?>> classNameSet = new HashSet<>(); 
    	
    	for(Join<?, ?> join:query.getJoins()) {
    		
    		if(!classNameSet.contains(join.getLeftEntity())) {
    			selectFieldList.addAll(prepareSelectFieldsForClass(join.getLeftEntity()));
    			classNameSet.add(join.getLeftEntity());
    		}
    		
    		if(!classNameSet.contains(join.getRightEntity())) {
    			selectFieldList.addAll(prepareSelectFieldsForClass(join.getRightEntity()));
    			classNameSet.add(join.getRightEntity());
    		}
    	}
    	
    	
    	String selectQuery = metamodel.buildSelectRequestWithSelectFieldMeta(selectFieldList);
    	QueryFragment whereQueryFragment = getQueryConditions(query.getCondition());
    	String joinSql = buildJoinSql(query.getJoins());
    	generatedSql = selectQuery + joinSql + " WHERE " + whereQueryFragment.getSql() ;
    	System.out.println("Generated SQL:"+generatedSql);
    	List<Object> params =  whereQueryFragment.getParams()
				.stream()
				.filter((param)->param!=null)
				.collect(Collectors.toList());
        return new QueryPlan(generatedSql, params, selectFieldList);
    }
    
    
    private List<SelectFieldMeta> prepareSelectFieldsForClass(Class<?> clss){
		List<SelectFieldMeta> list = new ArrayList<>();
		Metamodel metamodel = Metamodel.of(clss);
		String className = metamodel.getSimpleClassName();
		Map<String, Field> fieldMap = metamodel.getAllFieldMapping();
    	Iterator<String> fieldMapKeyIterator = fieldMap.keySet().iterator();
    	while(fieldMapKeyIterator.hasNext()) {
    		
    		String fieldName = fieldMapKeyIterator.next();
    		SelectFieldMeta fieldMeta = new SelectFieldMeta(clss, className+"."+fieldName, className+"_"+fieldName);
    		list.add(fieldMeta);
    	}
    	
    	return list;
    	
    	
    }
    
    
    private <T>String buildJoinSql(List<Join<?, ?>> joins) {
        if (joins == null || joins.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Join<?, ?> join : joins) {
            String joinKeyword = "";
            
            switch (join.getType()) {
            case INNER: joinKeyword = " INNER JOIN ";
            			break;
            case LEFT : joinKeyword = " LEFT JOIN ";
            			break;
            case RIGHT : joinKeyword = " RIGHT JOIN ";
        };
        String leftTableName = join.
					        		getLeftEntity().
					        		getSimpleName();
            String rightTableName =
                join.getRightEntity()
                    .getSimpleName();
            sb.append(joinKeyword)
              .append(rightTableName)
              .append(" ON ")
              .append(leftTableName+"."+join.getLeftField())
              .append(" = ")
              .append(rightTableName+"."+join.getRightField());
        }
        return sb.toString();
    }
    
    private QueryFragment getQueryConditions(Condition<?> condition) {
    	
    	if(condition == null) {
    		throw new IllegalArgumentException("Condition cannot be null");
    	}
    	
		Operator operator = condition.getOperator();
		Class<?> entityClass = condition.getEntityClass();
		String entityClassName = "";
		if(entityClass != null) {	
			entityClassName = entityClass.getSimpleName();
			metamodel = Metamodel.of(entityClass);
		}
		Object value = condition.getValue();
		String fieldName = condition.getField(); 
		
		Map<String, ColumnField> columnFields = metamodel.getNameFieldMapping();
		Field fld = columnFields.get(fieldName)!=null? columnFields.get(fieldName).getField(): null;
//		String returnValue = "";
//		boolean compositeOperator = false;
//		Object fieldValue = null;
		if (fld == null && operator != Operator.AND && operator != Operator.OR) {
		    throw new RuntimeException("Invalid field: " + fieldName);
		}
		
		
		if(value instanceof String) {
			value = "'"+value+"'";
		}
		
		switch(operator) {
		
		case EQ: 
			return new QueryFragment(entityClassName+"."+condition.getField() + " = ?", List.of(condition.getValue()));
			
		case GT: 
			return new QueryFragment(entityClassName+"."+condition.getField() + " > ?", List.of(condition.getValue()));
			
		case GTE:
			return new QueryFragment(entityClassName+"."+condition.getField() + " >= ?", List.of(condition.getValue()));
			
		case LT:
			return new QueryFragment(entityClassName+"."+condition.getField() + " < ?", List.of(condition.getValue()));
			
		case NE:
			return new QueryFragment(entityClassName+"."+condition.getField() + " != ?", List.of(condition.getValue()));
			
		case NN:
			return new QueryFragment(entityClassName+"."+condition.getField() + " IS NOT NULL ");
			
		case EN:
			return new QueryFragment(entityClassName+"."+condition.getField() + " IS NULL ");
			
		case LTE:
			return new QueryFragment(entityClassName+"."+condition.getField() + " <= ?", List.of(condition.getValue()));
		case IN:

		    Collection<?> values = (Collection<?>) condition.getValues();

		    String placeholders = values.stream()
		        .map(v -> "?")
		        .collect(Collectors.joining(", "));

		    return new QueryFragment(
		    		entityClassName+"."+fieldName + " IN (" + placeholders + ")",
		        new ArrayList<>(values)
		    );
		    
		case LIKE:

		    return new QueryFragment(
		    		entityClassName+"."+fieldName + " LIKE ?",
		        List.of(condition.getValue())
		    );
		case BETWEEN:

		    List<?> range = (List<?>) condition.getValues();

		    return new QueryFragment(
		    		entityClassName+"."+fieldName + " BETWEEN ? AND ?",
		        List.of(range.get(0), range.get(1))
		    );
		case AND:
			List<QueryFragment> andChildren = condition.getChildren()
            .stream()
            .map(this::getQueryConditions)
            .collect(Collectors.toList());
		    String andSql = "(" + andChildren.stream()
		            .map(QueryFragment::getSql)
		            .collect(Collectors.joining(" AND ")) + ")";
		    List<Object> andParams = andChildren.stream()
		            .flatMap(f -> f.getParams().stream())
		            .filter((val)->val!=null)
		            .collect(Collectors.toList());
		    return new QueryFragment(andSql, andParams);
			
		case OR:
			List<QueryFragment> orChildren = condition.getChildren()
            .stream()
            .map(this::getQueryConditions)
            .collect(Collectors.toList());
		    String orSql = "(" + orChildren.stream()
		            .map(QueryFragment::getSql)
		            .collect(Collectors.joining(" OR ")) + ")";
		    List<Object> orParams = orChildren.stream()
		            .flatMap(f -> f.getParams().stream())
		            .filter((val)->val!=null)
		            .collect(Collectors.toList());
		    return new QueryFragment(orSql, orParams);
			
		default:
		    throw new UnsupportedOperationException("Unsupported operator: " + operator);
		}
    	
    	
    }

	@Override
	public <T> List<JoinedRow> executeWithJoin(Query<T> query, Map<Class<?>, List<?>> entityListDataMap) {
		// TODO Auto-generated method stub
		QueryPlan plan = buildSqlWithJoin(query);
		
		
		return entityManager.returnJoinedRowAsPerQueryPlan(plan);
	}
}