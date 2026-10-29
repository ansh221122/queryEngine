import beanmanager.BeanManager;
import reflection.utility.EntityManager;
import reflection.utility.ManagedEntityManager;
import reflection.utility.Partner;
import reflection.utility.Person;
import reflection.utility.Vendor;
import provider.H2ConnectionProvider;
import provider.mysqlConnectorProvider;

public class CreateTable {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
		EntityManager<Partner> entityManager = beanManager.getInstance(ManagedEntityManager.class);	
		
		entityManager.createTable(Partner.class);

	}

}
