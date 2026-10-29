package reflection.utility;

import java.io.Serializable;

public class Address implements Serializable {
	private static final long serialVersionUID = 6529685098267757690L;
	@Column(name = "CITY")
	String city;
	@Column(name = "PINCODE")
	String pincode;
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPincode() {
		return pincode;
	}
	public void setPincode(String pincode) {
		this.pincode = pincode;
	}
	public Address(String city, String pincode) {
		super();
		this.city = city;
		this.pincode = pincode;
	}
	public Address() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "Address [city=" + city + ", pincode=" + pincode + "]";
	}
	
	
	

}
