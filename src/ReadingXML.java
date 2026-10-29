import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.IntStream;

import beanmanager.BeanManager;
import provider.xmlConnectionProvider;
import reflection.utility.EntityManager;
import reflection.utility.JoinType;
import reflection.utility.Node;
import reflection.utility.Partner;
import reflection.utility.Person;
import reflection.utility.Vendor;
import reflection.utility.Wrapper;
import reflection.utility.XMLEntityManager;
import util.GroupMetricsManager;
import util.MetricsManager;

public class ReadingXML {

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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		BeanManager beanManager = BeanManager.getManagerInstance(xmlConnectionProvider.class);
		EntityManager<Person> entityManager =  beanManager.getInstance(Person.class,XMLEntityManager.class);
		
		EntityManager<Vendor> entityManagerVendor =  beanManager.getInstance(XMLEntityManager.class);
		MetricsManager<Integer> metricsManager=entityManager.getMetrics(Integer.class, "age");
		GroupMetricsManager<String, Person> groupMetricsManager = entityManager.getGroupMetrics(String.class, "VENDOR_NAME", "getVendorName");
		
		Map<String, List<Person>> personListGroupByVendorName = groupMetricsManager.groupByColumn(); 
		System.out.println(groupMetricsManager.aggregateByGivenColumn("getName"));
		
		Iterator<String> keySetIterator = personListGroupByVendorName.keySet().iterator();
		while(keySetIterator.hasNext())
		{
			String vendorName = keySetIterator.next();
			System.out.println("for Vendor Name:"+vendorName);
			for(Person person:personListGroupByVendorName.get(vendorName))
			{
				System.out.println(person.getName());
			}
		}
		
		//System.out.println(personListGroupByAge);
		System.out.println(metricsManager.max());
		System.out.println(metricsManager.aggregateFunction((a,b)->a+b));
		System.out.println(groupMetricsManager.max());
		System.out.println(groupMetricsManager.aggregateFunction((a,b)->a+","+b));
		if("test".equals("test"))
			return;
		
		try {
			//Person p=entityManager.delete(Person.class, 2094891617);
			//System.out.println(p);
			Vendor MITSUBISHI = (Vendor) entityManagerVendor.find(Vendor.class, "VENDOR_NAME","MITSUBISHI").get(0);
			//Vendor MITSUBISHI=new Vendor(1542818514L, "MITSUBISHI", "LCD", "2000");
			//entityManagerVendor.delete(Vendor.class,1542818514L);
			//entityManagerVendor.persist(MITSUBISHI);
			System.out.println(MITSUBISHI);
			Person yungshung=new Person(25, "Nyonshi The Second", "MITSUBISHI", "Monu");
			//entityManager.searchAndUpdateDependency(yungshung);
			//entityManager.persist(yungshung);
			Stack<List<Wrapper>> stack = entityManagerVendor.connect(Vendor.class,JoinType.LEFT).connect(Person.class,JoinType.LEFT).filter((p)->p.getId()!=2145313929).returnList();
			
			System.out.println("stack size:::::"+stack.size());
			for(Wrapper obj:stack.pop())
			{
				Vendor  vendor=(Vendor) obj.getData();
				System.out.println(vendor.getVendorName());
			}
			
			for(Wrapper obj:stack.pop())
			{
				Person  person=(Person) obj.getData();
				System.out.println(person.getName()+"####"+obj.getParent().getData());
			}
			//entityManager.delete(Person.class, 2037640724L);
			//List<Object> list = entityManagerVendor.join(MITSUBISHI, Person.class);
			//Node node = entityManagerVendor.genericJoin(Vendor.class,Person.class);
			
			
			//System.out.println(node);
			//iterate(node);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
