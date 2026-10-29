package reflection.utility;

import java.util.List;

public class Person {
	@PrimaryKey(name="id")
	private long id;
	@metric
	@Column(name="age")
	@DisplayName(name="Age")
	private int age;
	@Column(name="name")
	@unique
	@DisplayName(name="Person name")
	private String name;
	@Column(name="VENDOR_ID")
	@update(classname="reflection.utility.Vendor", id="Vendor")
	@Inject(classname="reflection.utility.Vendor")
	private long vendorID;
	@pivot
	@metric
	@Column(name="VENDOR_NAME")
	private String vendorName;
	
	@Column(name="PARTNER_ID")
	@update(classname="reflection.utility.Partner",id = "partner")
	private long partnerID;
	@pivot
	@Column(name="PARTNER_NAME")
	@DisplayName(name="Partner Name")
	private String partnerName;
	public List<Partner> getPartners() {
		return partners;
	}
	public void setPartners(List<Partner> partners) {
		this.partners = partners;
	}
	@Column(name="ADDRESS")
	@DisplayName(name="Address")
	private Address addr;
	
	// a single person can have the multiple partners
	@relation
	private List<Partner> partners;
	
	
	
	
	public Address getAddr() {
		return addr;
	}
	public void setAddr(Address addr) {
		this.addr = addr;
	}
	public long getPartnerID() {
		return partnerID;
	}
	
	public void setPartnerID(long partnerID) {
		this.partnerID = partnerID;
	}
	@search(classname="reflection.utility.Partner", id="partner")
	public String getPartnerName() {
		return partnerName;
	}
	
	
	@setter(id ="partner")
	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}
	public long getVendorID() {
		return vendorID;
	}
	public void setVendorID(long vendorID) {
		this.vendorID = vendorID;
	}
	@Provides
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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

	public Person(int age, String name) {
		super();
		this.age = age;
		this.name = name;
	}
	
	public static Person of(int age, String name)
	{
		return new Person(age, name);
	}
	public Person() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Person(int age, String name, long vendorID, String vendorName) {
		super();
		this.age = age;
		this.name = name;
		this.vendorID = vendorID;
		this.vendorName = vendorName;
	}
	
	public Person(int age, String name, String vendorName, String partnerName) {
		super();
		this.age = age;
		this.name = name;
		this.vendorName = vendorName;
		this.partnerName = partnerName;
	}
	@search(classname="reflection.utility.Vendor", id="Vendor")
	public String getVendorName() {
		return vendorName;
	}
	@setter(id ="Vendor")
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String toString()
	{
		return "Person:: id-> "+id+", name->"+name+", age->"+age+", vendorID "+vendorID+", vendorName "+vendorName+", partnerID "+partnerID+", Address = "+addr;
	}

}
