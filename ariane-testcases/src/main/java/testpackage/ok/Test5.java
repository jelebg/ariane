package testpackage.ok;

// constructor
public class Test5 {

	static final String str = "test";
	
	public static String getStrStatic() {
		return str;
	}
	
	/*-Ariane
	CALL testpackage.ok.Test5.Test5() java.io.PrintStream.println(java.lang.String)
	CALL testpackage.ok.Test5.Test5() testpackage.ok.Test5.getStrStatic()
	CALL testpackage.ok.Test5.Test5() java.io.PrintStream.println(java.lang.String)
	*/
	public Test5() {
		System.out.println(str);
		System.out.println(getStrStatic());
	}
	
	/*-Ariane
	CALL testpackage.ok.Test5.Test5(testpackage.ok.String) java.io.PrintStream.println(java.lang.String)
	CALL testpackage.ok.Test5.Test5(testpackage.ok.String) testpackage.ok.Test5.getStrStatic()
	CALL testpackage.ok.Test5.Test5(testpackage.ok.String) java.io.PrintStream.println(java.lang.String)
	*/
	public Test5(String str2) {
		System.out.println(str);
		System.out.println(getStrStatic());
	}
	
	/*-Ariane
	CALL testpackage.ok.Test5.Test5(testpackage.ok.String,testpackage.ok.String) java.io.PrintStream.println(java.lang.String)
	CALL testpackage.ok.Test5.Test5(testpackage.ok.String,testpackage.ok.String) testpackage.ok.Test5.getStrStatic()
	CALL testpackage.ok.Test5.Test5(testpackage.ok.String,testpackage.ok.String) java.io.PrintStream.println(java.lang.String)
	*/
	public Test5(String str2, String str3) {
		System.out.println(str);
		System.out.println(getStrStatic());
	}

	/*-Ariane
	CALL testpackage.ok.Test5.test1(testpackage.ok.String,testpackage.ok.String) testpackage.ok.Test5.Test5()
	CALL testpackage.ok.Test5.test1(testpackage.ok.String,testpackage.ok.String) testpackage.ok.Test5.Test5(java.lang.String)
	CALL testpackage.ok.Test5.test1(testpackage.ok.String,testpackage.ok.String) testpackage.ok.Test5.Test5(java.lang.String,java.lang.String)
	*/
	public void test1(String str2, String str3) {
		Test5 t51 = new Test5();
		Test5 t52 = new Test5(str2);
		Test5 t53 = new Test5(str2, str3);

	}
	
}
