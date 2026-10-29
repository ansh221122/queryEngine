package util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupMetricsManager<T extends Comparable<T>, U> extends MetricsManager<T> {
	
	private List<U> entities = null;
	
	private String methodName = "";

	public GroupMetricsManager(List<T> data, Class<T> clss) {
		super(data, clss);
		// TODO Auto-generated constructor stub
	}
	
	public GroupMetricsManager(List<T> data,  Class<T> clss, List<U> entities) {
		
		super(data, clss);
		this.entities = entities;
		// TODO Auto-generated constructor stub
	}
	
	public GroupMetricsManager(List<T> data, Class<T> clss, List<U> entities, String methodName) {
		super(data, clss);
		this.entities = entities;
		this.methodName = methodName;
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("unchecked")
	public Map<T,List<U>> groupByColumn()
	{
		
		return
		(Map<T, List<U>>) entities.stream().collect(Collectors.groupingBy((u)->{
			try {
				return u.getClass().getDeclaredMethod(methodName).invoke(u);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			}
			return u;
		}));
		
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<?,T> aggregateByGivenColumn(String aggregateByColumn)
	{
		if(data.isEmpty())
			throw new RuntimeException("data is empty can't perform this operation. ");
		
		if(data.get(0) instanceof Integer){
			return
			(Map<?, T>) entities.stream().collect(Collectors.groupingBy((u)->{
				try {
					return u.getClass().getDeclaredMethod(aggregateByColumn).invoke(u);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					
					e.printStackTrace();
				}
				return u;
			}, Collectors.summingInt(u->{
				try {
					return (int) u.getClass().getDeclaredMethod(methodName).invoke(u);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					// TODO Auto-generated catch block
					
					e.printStackTrace();
				}
				return 0;
			})));
		
		}
		
		else if(data.get(0) instanceof Float || data.get(0) instanceof Double){
			return
					(Map<?, T>) entities.stream().collect(Collectors.groupingBy((u)->{
						try {
							return u.getClass().getDeclaredMethod(aggregateByColumn).invoke(u);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							// TODO Auto-generated catch block
							
							e.printStackTrace();
						}
						return u;
					}, Collectors.summingDouble(u->{
						try {
							return (int) u.getClass().getDeclaredMethod(methodName).invoke(u);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							// TODO Auto-generated catch block
							
							e.printStackTrace();
						}
						return 0;
					})));
		}
		else if(data.get(0) instanceof String) {
			
			return
					(Map<?, T>) entities.stream().collect(Collectors.groupingBy((u)->{
						try {
							return u.getClass().getDeclaredMethod(aggregateByColumn).invoke(u);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							// TODO Auto-generated catch block
							
							e.printStackTrace();
						}
						return u;
					}));
			
		}
		else {
			throw new RuntimeException("method applicable on int, float or double data type.");
		}
		
	}
	
	

}
