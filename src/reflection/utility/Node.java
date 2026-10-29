package reflection.utility;

import java.util.ArrayList;

public class Node {
	private int level;

	
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	private Object data;
	
	public Node() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Node(Object data) {
		super();
		this.data = data;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	private ArrayList<Node> nodes=new ArrayList();
	public void addToList(Node node)
	{
		nodes.add(node);
	}
	
	public int listSize()
	{
		return nodes.size();
	}
	
	public Node getfromList(int i)
	{
		return nodes.get(i);
	}
	
	
	public Node removeFromList(int i)
	{
		return nodes.remove(i);
	}
}
