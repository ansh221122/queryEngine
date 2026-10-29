import java.sql.SQLException;

import beanmanager.BeanManager;
import reflection.utility.EntityManager;
import reflection.utility.ManagedEntityManager;
import reflection.utility.Vendor;
import provider.H2ConnectionProvider;

public class UpdateObjeect {

	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(H2ConnectionProvider.class);
		EntityManager<Vendor> entityManageVendor = beanManager.getInstance(ManagedEntityManager.class);

		Vendor bmw=new Vendor(4, "BMW", "Engine", "8000");
		entityManageVendor.update(bmw);
		
	}

}
