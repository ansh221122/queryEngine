import java.sql.SQLException;

import beanmanager.BeanManager;
import provider.H2ConnectionProvider;
import provider.mysqlConnectorProvider;
import reflection.utility.EntityManager;
import reflection.utility.ManagedEntityManager;
import reflection.utility.Vendor;

public class UpdateTesting {

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
		EntityManager<Vendor> entityManageVendor = beanManager.getInstance(ManagedEntityManager.class);

		Vendor ferrari=new Vendor(4, "Ferrari", "ExhaustPipe", "8000");
		entityManageVendor.persist(ferrari);
		//entityManageVendor.update(bmw, "VENDOR_NAME");
		
			
	}

}
