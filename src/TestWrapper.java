import reflection.utility.Wrapper;

public class TestWrapper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		Student st = new Student(4,"ryan");
		Wrapper wrap = Wrapper.wrapIt(st);
		
		wrap
		.getConnections()
		.stream()
		.forEach((w)->System.out.println(w.getData()));

	}

}
