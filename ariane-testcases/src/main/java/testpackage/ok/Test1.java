package testpackage.ok;


// intra-class method calls

public class Test1 {

	String str = "test";
	public static final String strStatic = "test";

	public String getStr() {
		return str;
	}
	
	public static String getStrStatic() {
		return strStatic;
	}
	
	/*-Ariane
	 CALL testpackage.Test1.test1() java.io.PrintStream.println(java.lang.String)
	 CALL testpackage.Test1.test1() testpackage.Test1.getStr()
	 CALL testpackage.Test1.test1() java.io.PrintStream.println(java.lang.String)
	 CALL testpackage.Test1.test1() testpackage.Test1.getStr()
	 CALL testpackage.Test1.test1() java.io.PrintStream.println(java.lang.String)
	 */
	void test1() {
		System.out.println(str);
		System.out.println(getStr());
		System.out.println(this.getStr());
	}
	
	/*-Ariane
	 CALL testpackage.Test1.test2() java.io.PrintStream.println(java.lang.String)
	 CALL testpackage.Test1.test2() testpackage.Test1.getStr()
	 CALL testpackage.Test1.test2() java.io.PrintStream.println(java.lang.String)
	*/
	public void test2() {
		System.out.println(str);
		System.out.println(getStr());
	}
	
	/*-Ariane
	 CALL testpackage.Test1.test3() testpackage.Test1.getStrStatic()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStrStatic()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStrStatic()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStr()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStrStatic()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStr()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStrStatic()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStr()
	 CALL testpackage.Test1.test3() testpackage.Test1.getStrStatic()
	 */
	public void test3() {
		getStrStatic();
		this.getStrStatic();
		Test1.getStrStatic();
		String str = getStr()+getStrStatic();
		str = this.getStr()+this.getStrStatic();
		str = this.getStr()+Test1.getStrStatic();
	}

	/*-Ariane
     CALL testpackage.Test1.test4() testpackage.Test1.getStrStatic()
	 CALL testpackage.Test1.test4() testpackage.Test1.getStrStatic()
	 */
	public static void test4() {
		getStrStatic();
		Test1.getStrStatic();
	}


	
	
	
}
