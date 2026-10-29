import java.sql.SQLException;

import beanmanager.BeanManager;
import reflection.utility.EntityManager;
import reflection.utility.ManagedEntityManager;
import reflection.utility.Person;
import reflection.utility.RelationJoinManager;
import provider.mysqlConnectorProvider;

public class deleteRecords {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
		EntityManager<Person> entityManager = beanManager.getInstance(ManagedEntityManager.class);
		long id=1L;
		try {
			id=2L;
			Person p1=entityManager.delete(Person.class, id);
			System.out.println("record deleted::::"+p1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Requested record does not exist in table "+Person.class.getSimpleName());
			//e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
