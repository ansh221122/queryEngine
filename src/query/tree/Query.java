package query.tree;

import java.util.ArrayList;
import java.util.List;

public class Query<T> {

    private Class<T> entityClass;
    private Condition condition;
    private List<Join<?, ?>> joins = new ArrayList<>();

    private int limit;
    private int offset;

    // ---------- Builder ----------

    public static <T> Query<T> from(Class<T> entityClass) {
        Query<T> q = new Query<>();
        q.entityClass = entityClass;
        return q;
    }

    public Query<T> where(Condition condition) {
        this.condition = condition;
        return this;
    }

    public Query<T> join(Join<?, ?> join) {
        this.joins.add(join);
        return this;
    }

    public Query<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Query<T> offset(int offset) {
        this.offset = offset;
        return this;
    }

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public Condition getCondition() {
		return condition;
	}

	public List<Join<?, ?>> getJoins() {
		return joins;
	}

    // Getters...
}
