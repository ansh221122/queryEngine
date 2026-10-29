import java.sql.SQLException;

import beanmanager.BeanManager;
import provider.xmlConnectionProvider;
import reflection.utility.Address;
import reflection.utility.EntityManager;
import reflection.utility.Person;
import reflection.utility.Vendor;
import reflection.utility.XMLEntityManager;

public class WritingXMLTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(xmlConnectionProvider.class);
		EntityManager<Vendor> entityManager = beanManager.getInstance(XMLEntityManager.class);
		EntityManager<Person> entityManagerPerson = beanManager.getInstance(XMLEntityManager.class);
		try {
			Person p=entityManagerPerson.find(Person.class, 1866181391);
			p.getAddr().setCity("San Francisco");
			p.getAddr().setPincode("54312");
			System.out.println(p);
			entityManagerPerson.update(p);
			//entityManager.persist(vendor);
			//entityManagerPerson.searchAndUpdateDependency(p);
			//entityManagerPerson.persist(p);
			//entityManagerPerson.importFromCSV(Person.class, "D:\\xmlTest\\csvFeeder\\testing.csv");
			//System.out.println(entityManagerPerson.find(Person.class, 1409654608L).getAddr().getPincode());
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
