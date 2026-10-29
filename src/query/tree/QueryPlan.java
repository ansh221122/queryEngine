package query.tree;

import java.util.List;

public class QueryPlan {
    private String sql;
    private List<Object> params;
    private List<SelectFieldMeta> fieldMappings;
    
    public QueryPlan(String sql, List<Object> params, List<SelectFieldMeta> fieldMappings) {
		super();
		this.sql = sql;
		this.params = params;
		this.fieldMappings = fieldMappings;
	}
	public String getSql() {
        return sql;
    }
    public List<Object> getParams() {
        return params;
    }
    public List<SelectFieldMeta> getFieldMappings() {

        return fieldMappings;
    }
}