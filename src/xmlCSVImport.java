import beanmanager.BeanManager;
import provider.xmlConnectionProvider;
import reflection.utility.EntityManager;
import reflection.utility.Person;
import reflection.utility.XMLEntityManager;

public class xmlCSVImport {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BeanManager beanManager = BeanManager.getManagerInstance(xmlConnectionProvider.class);
		EntityManager<Person> entityManager = beanManager.getInstance(XMLEntityManager.class);
		//entityManager.importFromCSV(Person.class, "D:\\xmlTest\\csvFeeder\\testing.csv");
		String[][] data= {
				{"age","name","VENDOR_NAME","PARTNER_NAME"},
				{"21","sondel","BMW","lorry"},
				{"22","cruise","BMW","brett"},
				{"21","suzi","Brian","max"}	
				
		};
		
		try {
			System.out.println(entityManager.find(Person.class, 2094891617));
			entityManager.importFromArray(Person.class, data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
