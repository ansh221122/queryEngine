package query.tree;

public class SelectFieldMeta {
    private Class<?> entityClass;
    private String fieldName;
    private String columnAlias;
    public SelectFieldMeta(
            Class<?> entityClass,
            String fieldName,
            String columnAlias) {
        this.entityClass = entityClass;
        this.fieldName = fieldName;
        this.columnAlias = columnAlias;
    }
    public Class<?> getEntityClass() {
        return entityClass;
    }
    public String getFieldName() {
        return fieldName;
    }
    public String getColumnAlias() {
        return columnAlias;
    }
}
