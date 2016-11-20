package testpackage.ok;

// static initializer
public class Test3 {

	static final String str = "test";
	
	public static String getStrStatic() {
		return str;
	}
	
	{
		System.out.println(str);
		System.out.println(getStrStatic());
	}
	
}
