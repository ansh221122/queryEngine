package reflection.utility;

import java.util.List;

public class Partner {
	
	@PrimaryKey(name="PARTNER_ID")
	private long partnerID;
	@Column(name="PARTNER_NAME")
	@link
	@DisplayName(name="Partner Name")
	private String partnerName;
	@Column(name="PERSON_ID")
	@Inject(classname="reflection.utility.Person")
	private long personID;

	
	public long getPersonID() {
		return personID;
	}
	public void setPersonID(long personID) {
		this.personID = personID;
	}
	public long getPartnerID() {
		return partnerID;
	}
	public void setPartnerID(long partnerID) {
		this.partnerID = partnerID;
	}
	@Provides
	public String getPartnerName() {
		return partnerName;
	}
	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}
	public Partner( String partnerName) {
		super();
		
		this.partnerName = partnerName;
	}
	public Partner() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String toString()
	{
		return "Partner [ partnerID -> "+partnerID+", partnerName -> "+partnerName+" ]";
	}
	
	

}
