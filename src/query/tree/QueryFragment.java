package query.tree;
import java.util.*;
public class QueryFragment {
    private String sql;
    private List<Object> params;

    public QueryFragment(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params;
    }
    
    public QueryFragment(String sql) {
        this.sql = sql;
    }

    public String getSql() { return sql; }
    public List<Object> getParams() { return params; }

    public static QueryFragment of(String sql, Object... params) {
        return new QueryFragment(sql, Arrays.asList(params));
    }

    public static QueryFragment combine(String sql, List<QueryFragment> fragments) {
        List<Object> allParams = new ArrayList<>();
        for (QueryFragment f : fragments) {
            allParams.addAll(f.getParams());
        }
        return new QueryFragment(sql, allParams);
    }
}