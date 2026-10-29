import java.sql.SQLException;

import beanmanager.BeanManager;
import provider.xmlConnectionProvider;
import reflection.utility.EntityManager;
import reflection.utility.Person;
import reflection.utility.XMLEntityManager;

public class updateXML {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(xmlConnectionProvider.class);
		EntityManager<Person> entityManager = beanManager.getInstance(XMLEntityManager.class);
		Person p=new Person(21, "sam", "Brian", "jolly");
		p.setId(2094891617);
		try {
			entityManager.update(p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
