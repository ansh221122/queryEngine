import java.sql.SQLException;

import beanmanager.BeanManager;
import provider.mysqlConnectorProvider;
import reflection.utility.EntityManager;
import reflection.utility.Partner;
import reflection.utility.Person;
import reflection.utility.RelationJoinManager;

public class writingObjectsPartner {

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
		EntityManager<Partner> entityManager = beanManager.getInstance(RelationJoinManager.class);
		Partner george = new Partner("Ronnie");
		entityManager.persist(george);
	}

}
