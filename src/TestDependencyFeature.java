import beanmanager.BeanManager;
import provider.jsonConnectionProvider;
import provider.mysqlConnectorProvider;
import reflection.utility.EntityManager;
import reflection.utility.JsonEntityManager;
import reflection.utility.Person;
import reflection.utility.RelationJoinManager;

public class TestDependencyFeature {

	public static void main(String[] args) {
		
		BeanManager beanManager = BeanManager.getManagerInstance(jsonConnectionProvider.class);
		EntityManager<Person> entityManager = beanManager.getInstance(JsonEntityManager.class); 
		
		Person person = new Person();
		
		person.setName("Peter");
		person.setVendorName("Cisco");
		// update dependency feature works even if object to be persisted not present in DB it will fetch the dependency object primary key and update
		// the value in the field for main object this method can be called before persist
		System.out.println("Before dependency resolution: "+person);
		entityManager.searchAndUpdateDependency(person);
		System.out.println("After dependency resolution: "+person);
		person.setVendorName("");
		System.out.println("After dependency removel: "+person);
		entityManager.updateReverseDependency(person);
		System.out.println("After reverse dependency update: "+person);
		

	}

}
