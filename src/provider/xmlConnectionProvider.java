package provider;

import java.sql.Connection;
import java.sql.DriverManager;

import reflection.utility.IntegerFactory;
import reflection.utility.Provides;
import reflection.utility.RandomIntegerFactory;

public class xmlConnectionProvider {

	@Provides
	public String getDirectoryPath() throws Exception {
		
		return "D:\\xmlTest\\";
	}
	
	@Provides
	public IntegerFactory getIntegerFactory() throws Exception {
		return new RandomIntegerFactory();
	}
}
