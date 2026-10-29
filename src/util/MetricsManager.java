package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class MetricsManager<T extends Comparable<T>> {
	
	List<T> data = null;
	Class<T> clss;
	public MetricsManager(List<T> data, Class<T> clss)
	{
		this.data=data;
		this.clss = clss;
	}
	
	public T max()
	{
		return (T)Collections.max(data);
	}
	
	public T min()
	{
		return (T)Collections.min(data);
	}
	
	public T aggregateFunction(BinaryOperator<T> binaryOperator)
	{
		try {
		Optional<T> sum = data.stream().reduce(binaryOperator);
		return sum.orElse(null);
		}
		catch(Exception e)
		{
			System.out.println(this.getClass()+" --> Provided Operation not supported :: "+e);
		return null;
		}
	}
	
	public int length()
	{
		return data.size();
	}
	
	public List<T> sortedData()
	{
		List<T> temp = new ArrayList<>();
		data.stream().forEach((t)->temp.add(t));
		try
		{
			Collections.sort(temp);
		}
		catch(Exception e)
		{
			System.out.println("Data in list is not sortable :: "+e);
		}
		finally
		{
			return temp;
		}
	
	}
	
	
	public List<T> median()
	{
		List<T> medianList = new ArrayList<>();
		
		List<T> sortedList = sortedData();
		if((sortedList.size()&1) == 0) // if no of elements is odd
		{
			medianList.add(sortedList.get((sortedList.size()/2)+1));
			medianList.add(sortedList.get(sortedList.size()/2));

		}else
		{
			medianList.add(sortedList.get(sortedList.size()/2));
		}
			return medianList;
		
	}
	
	public List<T> percentile()
	{
		
		List<T> sortedData = sortedData();
		List<T> percentileList = new ArrayList<>();
		int n = sortedData.size();
		
		if(n<=4)
			return sortedData;
		
		for(int i=0;i<sortedData.size();i++)
		{
			float dataPercentile = ((i*100)/n);
			if(dataPercentile>=25.0 && dataPercentile<=35.0)
			{
				percentileList.add(sortedData.get(i));
			}
			
			if(dataPercentile>=45.0 && dataPercentile<=55.0)
			{
				percentileList.add(sortedData.get(i));
			}
			
			if(dataPercentile>=65.0 && dataPercentile<=75.0)
			{
				percentileList.add(sortedData.get(i));
			}
			
			
		}
		
		return percentileList;
	}
	
	
	
	

}
