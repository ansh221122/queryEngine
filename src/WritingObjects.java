import java.sql.SQLException;
import java.util.Scanner;

import beanmanager.BeanManager;
import reflection.utility.Address;
import reflection.utility.EntityManager;
import reflection.utility.ManagedEntityManager;
import reflection.utility.Person;
import reflection.utility.RelationJoinManager;
import reflection.utility.Student;
import reflection.utility.Vendor;
import provider.H2ConnectionProvider;
import provider.mysqlConnectorProvider;

public class WritingObjects {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//EntityManager<Person> entityManager=EntityManager.of(Person.class);
		//EntityManager<Student> entityManagerStudent=EntityManager.of(Student.class);
		BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
		EntityManager<Person> entityManager = beanManager.getInstance(RelationJoinManager.class);
		
		EntityManager<Vendor> entityManagerVendor = beanManager.getInstance(ManagedEntityManager.class);

		Scanner sc=new Scanner(System.in);
		//System.out.println("enter data for person in order::age::name::vendorID::vendorName");
		Person p=new Person(38, "Samuel White", "Donald","Ronnie");
		p.setAddr(new Address("colorado", "54324"));
		//entityManager.searchAndUpdateDependency(p);
		//System.out.println(entityManager.find(Person.class, 9L).getAddr().getCity());
		//System.out.println(entityManager.importFromCSV(Person.class, "D:\\xmlTest\\csvFeeder\\testing.csv"));
		entityManager.persist(p);
		
		//entityManager.find(Person.class, (p1)->p1.getName()!=null);
		
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}

}
