import beanmanager.BeanManager;

import provider.xmlConnectionProvider;
import reflection.utility.EntityManager;
import reflection.utility.Person;
import reflection.utility.Vendor;
import reflection.utility.XMLEntityManager;

public class creatingXML {

	public static void main(String... args)
	{
		BeanManager beanManager = BeanManager.getManagerInstance(xmlConnectionProvider.class);
		EntityManager<Vendor> entityManager = beanManager.getInstance(XMLEntityManager.class);
		
		entityManager.createTable(Vendor.class);
	
	}
	
	
}
