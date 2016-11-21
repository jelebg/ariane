package testpackage.ok;

// static initializer
public class Test3 {

	static final String str = "test";
	
	/*-Ariane
	CALL testpackage.ok.Test3.<init>() java.io.PrintStream.println(java.lang.String)
	CALL testpackage.ok.Test3.<init>() testpackage.ok.Test3.getStrStatic()
	CALL testpackage.ok.Test3.<init>() java.io.PrintStream.println(java.lang.String)
	CALL testpackage.ok.Test3.<static_init() java.io.PrintStream.println(java.lang.String)
	CALL testpackage.ok.Test3.<static_init() testpackage.ok.Test3.getStrStatic()
	CALL testpackage.ok.Test3.<static_init() java.io.PrintStream.println(java.lang.String)
	 */
	public static String getStrStatic() {
		return str;
	}
	
	{
		System.out.println(str);
		System.out.println(getStrStatic());
	}
	
	static {
		System.out.println(str);
		System.out.println(getStrStatic());
	}
	
	
}
