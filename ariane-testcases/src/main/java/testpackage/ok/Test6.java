package testpackage.ok;

// conversion
public class Test6 {

	/*-Ariane
	CALL testpackage.ok.Test6.test1(testpackage.ok.String,testpackage.ok.String) java.lang.String.substring(int, int)
    CALL testpackage.ok.Test6.test1(testpackage.ok.String,testpackage.ok.String) java.lang.Integer.toString(int)
    CALL testpackage.ok.Test6.test1(testpackage.ok.String,testpackage.ok.String) java.lang.String.substring(int)
    CALL testpackage.ok.Test6.test1(testpackage.ok.String,testpackage.ok.String) java.lang.Integer.toString(int)
    CALL testpackage.ok.Test6.test1(testpackage.ok.String,testpackage.ok.String) java.lang.String.substring(int)
    CALL testpackage.ok.Test6.test1(testpackage.ok.String,testpackage.ok.String) java.lang.String.substring(int)
	*/
	public void test1(String str2, String str3) {
		Integer i = 0;
		int j = 0;
		String str = null;
		String str21 = str.substring(i,  j);
		Integer.toString(i).substring(i);
		Integer.toString(i).substring((int) 1.02);
		"".substring(i);
	}
	
}
