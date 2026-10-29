package reflection.utility;

public class Student {
	
	public Student( int clas, int age, String name) {
		super();
				this.clas = clas;
		this.age = age;
		this.name = name;
	}
	@PrimaryKey(name="id")
	private long id;
	@Column(name="class")
	private int clas;
	@Column(name="age")
	private int age;
	@Column(name="name")
	private String name;
	public long getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getClas() {
		return clas;
	}
	public void setClas(int clas) {
		this.clas = clas;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String toString()
	{
		return "Student:: id->"+id+", class->"+clas+", name->"+name+", age->"+age;
	}
	
}
