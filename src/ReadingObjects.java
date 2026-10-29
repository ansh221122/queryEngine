import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mysql.jdbc.IterateBlock;

import beanmanager.BeanManager;
import reflection.utility.EntityManager;
import reflection.utility.JoinType;
import reflection.utility.ManagedEntityManager;
import reflection.utility.Node;
import reflection.utility.Partner;
import reflection.utility.Person;
import reflection.utility.RelationJoinManager;
import reflection.utility.Vendor;
import reflection.utility.Wrapper;
import util.MetricsManager;
import provider.H2ConnectionProvider;
import provider.mysqlConnectorProvider;
import query.tree.Condition;
import query.tree.Join;
import query.tree.JoinedRow;
import query.tree.Query;

public class ReadingObjects {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		BeanManager beanManager = BeanManager.getManagerInstance(mysqlConnectorProvider.class);
		EntityManager<Vendor> entityManager = beanManager.getInstance(Vendor.class,RelationJoinManager.class); 
		
		final String VENDOR_ID = "VENDOR_ID";
		final String VENDOR_NAME = "VENDOR_NAME";
		Condition<?> condition = Condition.and(Condition.greaterThan(Person.class, "age", 20),
												Condition.lessThan(Person.class, "age", 30),
												Condition.equals(Vendor.class, VENDOR_NAME, "SIMON"));
		
		Query<Vendor> query = Query.from(Vendor.class)
									.where(condition)
									.join(new Join<Vendor, Person>(Vendor.class, Person.class, VENDOR_ID, VENDOR_ID, JoinType.LEFT));
		List<JoinedRow> data = entityManager.findWithJoin(query);
		data.forEach((joinedRow)->{
			System.out.println("Person data -> "+joinedRow.get(Person.class));
		System.out.println("Vendor data -> "+joinedRow.get(Vendor.class));						
		
		});
//		MetricsManager<Integer> metricsManager = entityManager.getMetrics(Integer.class, "age");
//		System.out.println(metricsManager.max());
//		System.out.println(metricsManager.aggregateFunction((a,b)->a+b));
//		
//		EntityManager<Vendor> entityManagerVendor = beanManager.getInstance(Vendor.class,RelationJoinManager.class); 
//		
//		System.out.println("beanManager Hashcode -->"+beanManager.hashCode());
//		System.out.println(metricsManager.sortedData());
//		System.out.println(metricsManager.median());
//		System.out.println(metricsManager.percentile());
//		
//		List<Person> listWithNameUpperCase = entityManager.findAndProcess(Person.class, (p)->p!=null, (p)->{
//			p.setName(p.getName().toUpperCase());
//			return p;
//			});
//		
//		listWithNameUpperCase.forEach((p)->System.out.println(p.getName()));
//		
//		System.out.println(entityManager.structuredData(Join.LEFT, Vendor.class, Person.class, Partner.class));
		if("test".equals("test"))
			return;
//		Person john= entityManager.find(Person.class, 1L);
//		
//		System.out.println("before reverse dependency backtrack set::"+john);
//		entityManager.updateReverseDependency(john);
//		System.out.println("after reverse dependency backtrack set::"+john);
//		
//		String[] columns = {"VENDOR_NAME","PARTNER_NAME"};
//		List<Person> list = entityManager.pivotColumn(Person.class, columns);
//		
//		System.out.println(list);
//		
//		Vendor vendor2 = entityManagerVendor.find(Vendor.class, 1L);
//		List<Person> persons=vendor2.getPersons();
//		persons.stream().forEach((person)->System.out.println(person+"<<<<>>>>"+person.getPartners()));
//		
//		if("test".equals("test"))
//			return;
		//System.out.println(entityManager.find(Person.class, (t)->t.getVendorName()!=null?t.getVendorName().equals("Donald"):false));
		
		Stack<List<Wrapper>> stack;//=entityManager.link(Vendor.class).link(Person.class).filter((p)->((Person)p).getVendorName()!=null).link(Partner.class).returnList();
		stack=entityManager.connect(Vendor.class,JoinType.LEFT).connect(Person.class,JoinType.LEFT).filter((p)->(p).getPartnerName()!=null && p.getAge()>=30 ).connect(Partner.class,JoinType.LEFT).filter((p)->p.getPartnerName().equals("jerk")).returnList();
		for(Wrapper obj:stack.pop())
		{
			System.out.println("######Vendors##########");
			Vendor  vendor= (Vendor) obj.getData();
			System.out.println(vendor.getVendorName());
			System.out.println("Persons connected>>>>>>>>");
			//obj.getConnections().stream().forEach((w)->w.);
			obj.getConnections().stream().forEach((w)->((Wrapper) w).getData());
		}
		
		List<Wrapper> personList = stack.pop();
		
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		personList.forEach((w)->{System.out.println(w.getData());
								w.getConnections().stream().forEach((w2)->System.out.println(((Wrapper) w2).getData()));
		
								});
		
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		
		Map<String,List<Wrapper>> personMap=personList.stream()
				.collect(Collectors.groupingBy(w->((Person)w.getData()).getVendorName()==null?"":((Person)w.getData()).getVendorName()));
		personMap.forEach((vendor,person)->{System.out.println(vendor);
	
		person.forEach((w)->System.out.println(w.getData()));
		});
		
		List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
		Integer sum = integers.stream().filter((i)->i>=3)
		  .mapToInt((d)->d).sum();
		System.out.println("the sum:--------------------:-"+sum);
		sum = integers.stream().filter((i)->i>=3)
				  .mapToInt((d)->d).reduce(1, (a,b)->a*b);
		
		System.out.println("the sum:--------------------:-"+sum);
		
		Optional<Wrapper> oldestPerson = personList.stream().max(Comparator.comparing(w->((Person)w.getData()).getAge()));
		System.out.println("Oldest Person::::"+oldestPerson.get().getData());
		String buffertext ="######Partners##########";
		System.out.println(buffertext);
		for(Wrapper obj:stack.pop())
		{
			Partner  partner=(Partner) obj.getData();

			//System.out.println();
			System.out.print(partner.getPartnerName());
			IntStream.range(0, buffertext.length()-partner.getPartnerName().length()-1).forEach((i)->System.out.print(" "));
			System.out.print("#");
			System.out.println();
		}
		//EntityManager<Person> entityManager=EntityManager.of(Person.class); // for closely coupled connection object uncomment this code
		///entityManager.importFromCSV(Person.class, "D:\\xmlTest\\csvFeeder\\testing.csv");
//		System.out.println("entityManager1 ::::::::"+entityManager);
//		//Vendor donald = entityManager.find(Vendor.class,1L);
//		Person john=entityManager.find(Person.class,1L);
//		System.out.println(john);
//		john.setId(0L);
//		john.setPartnerName("Ronnie");
//		entityManager.searchAndUpdateDependency(john);
//		//john.setAge(34);
//		entityManager.persist(john);
//		
//		System.out.println(john);
////		entityManager = beanManager.getInstance(ManagedEntityManager.class);
////		System.out.println("entityManager1 ::::::::"+entityManager);
////		System.out.println(entityManager.find(Person.class,2L));
//		
//		EntityManager<Vendor> entityManagerVendor = beanManager.getInstance(RelationJoinManager.class); 
//		Vendor donald =entityManagerVendor.find(Vendor.class, 1L);
//		System.out.println("vendor donald"+donald);
//		List list= entityManagerVendor.join(donald, Person.class);
//		Map valueMap=entityManagerVendor.genericJoin(Vendor.class, Person.class);
//		Node root=entityManagerVendor.genericJoin(Vendor.class,Person.class,Partner.class);
//		iterate(root);
//		Arrays.stream(list.toArray())
//		.forEach(System.out::println);
//		
//		Arrays.stream(list.toArray())
//		.forEach(System.out::println);
//		Iterator it = valueMap.keySet().iterator();
//		while(it.hasNext())
//		{
//			Object o=it.next();
//			System.out.println(o+"-->"+valueMap.get(o));
//		}
//	}
//	
//	public static void iterate(Node node)
//	{
//		IntStream.range(0, node.getLevel()).forEach((I)->System.out.print("\t"));
//		System.out.println("data at level("+node.getLevel()+")-->"+node.getData());
//		if(node.listSize()==0)
//			return;
//		
//		for(int i=0;i<node.listSize();i++)
//		{
//			iterate(node.getfromList(i));
//		}
		testPredicate((t)->((String)t).charAt(0)=='h');
	}
	
	public static void testPredicate(Predicate predicate) {
		System.out.println(predicate.test("hello"));
	}

}
