import java.sql.SQLException;
import reflection.utility.Wrapper;
import java.util.List;
import java.util.Stack;

import beanmanager.BeanManager;
import provider.csvConnectionProvider;
import provider.jsonConnectionProvider;
import reflection.utility.CSVEntityManager;
import reflection.utility.EntityManager;
import reflection.utility.JoinType;
import reflection.utility.JsonEntityManager;
import reflection.utility.Person;
import reflection.utility.Vendor;

public class writeCsvObjects {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(csvConnectionProvider.class);
		//EntityManager<Person> entityManagerPerson = beanManager.getInstance(CSVEntityManager.class);
		EntityManager<Vendor> entityManagerVendor = beanManager.getInstance(CSVEntityManager.class);
		Person john =null;
//		try {
//			john = entityManagerPerson.find(Person.class, 802489153L);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(john);
//		entityManagerPerson.searchAndUpdateDependency(john);
//		System.out.println(john);
//		entityManagerPerson.persist(john);
		Vendor donald = new Vendor(0, "Donald", "LawnMower", "2000");
		entityManagerVendor.persist(donald);
//		Vendor dabur = new Vendor(0, "Dabur", "Honey", "150");
		
//		Stack<List<Wrapper>> stack= entityManagerPerson.connect(Vendor.class,Join.LEFT).filter((v)->Integer.parseInt(v.getProductCost())<1000).connect(Person.class,Join.LEFT).returnList();
//		for(Wrapper wrap:stack.pop())
//		{
//			Vendor  vendor=(Vendor)wrap.getData(); 
//			System.out.println(vendor);
//		}
//		
//		for(Wrapper wrap:stack.pop())
//		{
//			Person  person=(Person)wrap.getData(); 
//			System.out.println(person);
//			
//		}
//		
//		List list = entityManagerPerson.find(Person.class, "VENDOR_NAME", "Donald");
//		System.out.println("**************************");
//		System.out.println(list);
		
		//entityManagerPerson.persist(john);

		//entityManagerVendor.persist(donald);
		//entityManagerVendor.persist(dabur);
		//entityManagerPerson.searchAndUpdateDependency(john);
		//entityManagerPerson.persist(john);
		
	}

}
