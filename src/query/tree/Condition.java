package query.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Condition<T> {

    private Operator operator;

    // Entity ownership
    private Class<T> entityClass;

    // For leaf nodes
    private String field;

    private Object value;

    private Collection<?> values;

    // For composite nodes (AND / OR)
    private List<Condition<?>> children;

    private Condition() {}

    // =========================================================
    // STATIC BUILDERS
    // =========================================================

    public static <T> Condition<T> equals(
            Class<T> cls,
            String field,
            Object value) {

        return leaf(
                cls,
                Operator.EQ,
                field,
                value
        );
    }

    public static <T> Condition<T> notEquals(
            Class<T> cls,
            String field,
            Object value) {

        return leaf(
                cls,
                Operator.NE,
                field,
                value
        );
    }

    public static <T> Condition<T> greaterThan(
            Class<T> cls,
            String field,
            Object value) {

        return leaf(
                cls,
                Operator.GT,
                field,
                value
        );
    }

    public static <T> Condition<T> greaterThanOrEqualsTo(
            Class<T> cls,
            String field,
            Object value) {

        return leaf(
                cls,
                Operator.GTE,
                field,
                value
        );
    }

    public static <T> Condition<T> lessThan(
            Class<T> cls,
            String field,
            Object value) {

        return leaf(
                cls,
                Operator.LT,
                field,
                value
        );
    }
    
    public static <T> Condition<T> notNull(
            Class<T> cls,
            String field) {

        return leaf(
                cls,
                Operator.NN,
                field,
                new Object()
        );
    }
    
    public static <T> Condition<T> isNull(
            Class<T> cls,
            String field) {

        return leaf(
                cls,
                Operator.EN,
                field,
                new Object()
        );
    }

    public static <T> Condition<T> lessThanOrEqualsTo(
            Class<T> cls,
            String field,
            Object value) {

        return leaf(
                cls,
                Operator.LTE,
                field,
                value
        );
    }

    public static <T> Condition<T> like(
            Class<T> cls,
            String field,
            String pattern) {

        return leaf(
                cls,
                Operator.LIKE,
                field,
                pattern
        );
    }

    public static <T> Condition<T> in(
            Class<T> cls,
            String field,
            Collection<?> values) {

        return leaf(
                cls,
                Operator.IN,
                field,
                values
        );
    }

    public static <T> Condition<T> between(
            Class<T> cls,
            String field,
            Object start,
            Object end) {

        return leaf(
                cls,
                Operator.BETWEEN,
                field,
                Arrays.asList(start, end)
        );
    }

    // =========================================================
    // LEAF BUILDERS
    // =========================================================

    private static <T> Condition<T> leaf(
            Class<T> cls,
            Operator op,
            String field,
            Object value) {

        Condition<T> c = new Condition<>();

        c.entityClass = cls;
        c.operator = op;
        c.field = field;
        c.value = value;

        return c;
    }

    private static <T> Condition<T> leaf(
            Class<T> cls,
            Operator op,
            String field,
            Collection<?> values) {

        Condition<T> c = new Condition<>();

        c.entityClass = cls;
        c.operator = op;
        c.field = field;
        c.values = values;

        return c;
    }

    // =========================================================
    // COMPOSITE BUILDERS
    // =========================================================

    public static Condition<?> and(
            Condition<?>... conditions) {

        return composite(
                Operator.AND,
                conditions
        );
    }

    public static Condition<?> or(
            Condition<?>... conditions) {

        return composite(
                Operator.OR,
                conditions
        );
    }

    private static Condition<?> composite(
            Operator op,
            Condition<?>... conditions) {

        Condition<?> c = new Condition<>();

        c.operator = op;
        c.children = Arrays.asList(conditions);

        return c;
    }

    // =========================================================
    // FLUENT API
    // =========================================================

    public Condition<?> and(
            Condition<?> other) {

        return Condition.and(this, other);
    }

    public Condition<?> or(
            Condition<?> other) {

        return Condition.or(this, other);
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public Operator getOperator() {
        return operator;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public Collection<?> getValues() {
        return values;
    }

    public List<Condition<?>> getChildren() {
        return children;
    }
}
