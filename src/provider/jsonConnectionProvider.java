package provider;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import reflection.utility.IntegerFactory;
import reflection.utility.Provides;

public class jsonConnectionProvider {

	@Provides
	public String getDirectoryPath() throws Exception {
		
		//return "D:\\xmlTest\\";
		return "D:\\jsonTest\\";
	}
	
}
