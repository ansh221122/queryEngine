import java.util.Arrays;
import java.util.List;

import beanmanager.BeanManager;
import provider.mysqlConnectorProvider;
import reflection.utility.EntityManager;
import reflection.utility.ManagedEntityManager;
import reflection.utility.RelationJoinManager;
import reflection.utility.Vendor;

public class ReadTesting {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
		EntityManager<Vendor> entityManageVendor = beanManager.getInstance(RelationJoinManager.class);
		List objectList= entityManageVendor.find(Vendor.class, "PRODUCT_COST", "8000");
		Arrays.stream(objectList.toArray()).forEach(System.out::println);
	}

}
