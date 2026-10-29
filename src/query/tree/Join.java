package query.tree;

import reflection.utility.JoinType;

public class Join<L, R> {

    private Class<L> leftEntity;
    private Class<R> rightEntity;

    private String leftField;
    private String rightField;

    private JoinType type;

    public Join(Class<L> leftEntity, Class<R> rightEntity,
                String leftField, String rightField,
                reflection.utility.JoinType type) {
        this.leftEntity = leftEntity;
        this.rightEntity = rightEntity;
        this.leftField = leftField;
        this.rightField = rightField;
        this.type = type;
    }

	public Class<L> getLeftEntity() {
		return leftEntity;
	}


	public Class<R> getRightEntity() {
		return rightEntity;
	}


	public String getLeftField() {
		return leftField;
	}

	public String getRightField() {
		return rightField;
	}


	public JoinType getType() {
		return type;
	}

 
}
