import java.sql.SQLException;
import reflection.utility.Wrapper;
import java.util.List;
import java.util.Stack;
import java.util.stream.IntStream;

import beanmanager.BeanManager;
import provider.jsonConnectionProvider;
import provider.xmlConnectionProvider;
import query.tree.Condition;
import query.tree.Join;
import query.tree.JoinedRow;
import query.tree.Query;
import reflection.utility.Address;
import reflection.utility.EntityManager;
import reflection.utility.JoinType;
import reflection.utility.JsonEntityManager;
import reflection.utility.Node;
import reflection.utility.Person;
import reflection.utility.Vendor;
import reflection.utility.XMLEntityManager;

public class writingJsonTest {
	
	public static void iterate(Node node)
	{
		IntStream.range(0, node.getLevel()).forEach((I)->System.out.print("\t"));
		System.out.println("data at level("+node.getLevel()+")-->"+node.getData());
		if(node.listSize()==0)
			return;
		
		for(int i=0;i<node.listSize();i++)
		{
			iterate(node.getfromList(i));
		}
	}

	public static void main(String... strings ) throws Exception
	{
	BeanManager beanManager = BeanManager.getManagerInstance(jsonConnectionProvider.class);
	String[][] objects = {
			{"VENDOR_NAME","PRODUCT_DESCRIPTION","PRODUCT_COST"},
			{"Bosch","Lamps","240.00"}
			
	};
	EntityManager<Vendor> entityManager = beanManager.getInstance(JsonEntityManager.class);
	
	EntityManager<Person> entityManagerPerson =beanManager.getInstance(Person.class, JsonEntityManager.class);
	//Person p1 = entityManagerPerson.find(Person.class, (p)->p.getName().equals("Sam Altman")).get(0);// list of person class objects through predicate search
	//System.out.println(p1);
	
//	Person p2 = new Person(45, "Sam Altman", 0L, "OpenAI");
//	Address addr = new Address();
//	addr.setCity("San Francisco");
//	addr.setPincode("119942");
//	p2.setAddr(addr);
//	entityManagerPerson.persist(p2);
	
	
	
	Condition<Person> condition =Condition.notNull(Person.class, "name");
	
	Query<Person> query = Query.from(Person.class).where(condition).join(new Join<>(Person.class, Vendor.class, "VENDOR_ID", "VENDOR_ID", JoinType.LEFT));
	List<JoinedRow> data = entityManagerPerson.findWithJoin(query);
	System.out.println(data);
	data.forEach((joinedRow)->{
		System.out.println("Person data -> "+joinedRow.get(Person.class));
	System.out.println("Vendor data -> "+joinedRow.get(Vendor.class));						
	
	});
	
	
//	
//	
//	
//	Person p2 = new Person(42, "Samuel", 0L, "Cisco");
//	Address addr = new Address();
//	addr.setCity("Washington DC");
//	addr.setPincode("241265");
//	p2.setAddr(addr);
//	System.out.println(p2);
//	entityManagerPerson.persist(p2);
//	
//	
//	//List<Person> list= entityManagerPerson.find(Person.class,(t)->t.getAge()>25 && t.getName().equals("ron") );
//	entityManager.importFromArray(Vendor.class, objects);
//	Stack<List<Wrapper>> stack = entityManager.connect(Vendor.class,Join.LEFT).connect(Person.class,Join.LEFT).returnList();
//	List<Wrapper> list = entityManager.connect(Vendor.class,Join.LEFT).connect(Person.class,Join.LEFT).returnSingleEntityObjects(Person.class);
//	
//	//System.out.println("Vendor::::"+stack.pop());
//	list.stream().forEach((w)->System.out.println("->>>>>>"+w.getData()));
//	System.out.println("******************************");
//	
//	
//	for(Wrapper obj:stack.pop())
//	{
//		Vendor  vendor=(Vendor) obj.getData();
//		System.out.println(vendor.getVendorName());
//	}
//	
//	for(Wrapper obj:stack.pop())
//	{
//		Person  person=(Person) obj.getData();
//		System.out.println(person.getName()+"####"+obj.getParent().getData());
//	}
	//System.out.println("Person::::"+stack.pop());
	//System.out.println(list);
//	Person person = new Person(24, "Darlene", "Cisco", "infotek");
//	Vendor cisco = new Vendor(0, "Cisco", "Rounter", "2000");
//	entityManager.persist(cisco);
//	
//	//Person person = entityManagerPerson.find(Person.class, 529914971L);
//	//System.out.println(person);
//	person.setId(557396531L);
//	entityManagerPerson.update(person);
//	
//	Node node=entityManager.genericJoin(Vendor.class,Person.class);
//	iterate(node);
	
			//Person person = new Person(24, "ron", "richelle", "monu");
	//entityManagerPerson.persist(person);
	
	
	}
	}
