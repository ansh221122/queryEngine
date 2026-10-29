package reflection.utility;

import java.util.List;

public class Wrapper {
	private Wrapper parent=null;
	public int childCount = 0;
	private Object data =null;
	private List<Wrapper> wrapList = null;
	private List<Wrapper> connections = null;
	private JoinType joinType = null;
	
	
	public JoinType getJoin() {
		return joinType;
	}
	
	public void setJoin(JoinType joinType) {
		this.joinType = joinType;
	}
	public List<Wrapper> getConnections() {
		return connections;
	}
	public void setConnections(List<Wrapper> connections) {
		this.connections = connections;
	}
	public List<Wrapper> getWrapList() {
		
		return wrapList;
	}
	public void setWrapList(List<Wrapper> wrapList) {
		this.wrapList = wrapList;
	}
	private Wrapper()
	{
		
	}
	public static Wrapper wrapIt(Object data)
	{
		return new Wrapper(data);
	}
	
	

	
	private Wrapper(Object data)
	{
		this.data=data;
	}
	public Wrapper getParent() {
		return parent;
	}
	public void setParent(Wrapper parent) {
		this.parent = parent;
	}
	public int getCount() {
		return childCount;
	}
	public void setCount(int count) {
		this.childCount = count;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
}
