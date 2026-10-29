
public class Student {
	
	private int rNo;
	private String name;
	
	public Student()
	{
		
	}
	
	
	public Student(int rNo, String name) {
		super();
		this.rNo = rNo;
		this.name = name;
	}


	public int getrNo() {
		return rNo;
	}
	public void setrNo(int rNo) {
		this.rNo = rNo;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	

}
