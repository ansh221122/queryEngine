package provider;

import reflection.utility.Provides;

public class csvConnectionProvider {
	@Provides
	public String getDirectoryPath() throws Exception {
		
		//return "D:\\xmlTest\\";
		return "D:\\csvTest\\WithHashSeparator\\";
	}
	
	@Provides
	public char getSeparator() throws Exception {
		
		//return "D:\\xmlTest\\";
		return '#';
	}
	
	
}
