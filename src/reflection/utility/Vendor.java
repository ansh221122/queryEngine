package reflection.utility;

import java.util.ArrayList;
import java.util.List;

public class Vendor {

	@PrimaryKey(name="VENDOR_ID")
	private long vendorID;
	@link
	@unique
	@Column(name="VENDOR_NAME")
	@DisplayName(name="Vendor Name")
	private String vendorName;
	@Column(name="PRODUCT_DESCRIPTION")
	@DisplayName(name="Product Description")
	private String productDescription;
	@DisplayName(name="Product Cost")
	@Column(name="PRODUCT_COST")
	private String productCost;
	
	// multiple persons can have the same vendors
	@relation
	private List<Person> persons=new ArrayList<>();
	
	
	public List<Person> getPersons() {
		return persons;
	}
	public void setPersons(List<Person> persons) {
		this.persons = persons;
	}
	public Vendor() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Vendor(long vendorID, String vendorName, String productDescription, String productCost) {
		super();
		this.vendorID = vendorID;
		this.vendorName = vendorName;
		this.productDescription = productDescription;
		this.productCost = productCost;
	}
	@Provides
	public long getVendorID() {
		return vendorID;
	}
	public void setVendorID(long vendorID) {
		this.vendorID = vendorID;
	}
	
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	public String getProductCost() {
		return productCost;
	}
	public void setProductCost(String productCost) {
		this.productCost = productCost;
	}
	
	public String toString()
	{
		return "Vendor::::"+vendorName+", "+productDescription+", "+productCost;
	}
}
