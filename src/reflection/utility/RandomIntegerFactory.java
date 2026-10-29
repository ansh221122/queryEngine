package reflection.utility;

import java.util.concurrent.ThreadLocalRandom;

public class RandomIntegerFactory implements IntegerFactory {

	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;
	
	public static int getRandomInteger()
	{
		int newID = ThreadLocalRandom.current().nextInt();
		if(newID<0)
			newID *=-1;
		return newID;
	}

	@Override
	public int returnRandomInteger() {
		// TODO Auto-generated method stub

		return getRandomInteger();
	}
	

}
