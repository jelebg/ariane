package testpackage.ok;

import testpackage.ok.Test1;

// appels inter-classes

public class Test2 {
	Test1 t1 = null;
	
	/*-Ariane
	 CALL testpackage.ok.Test2.test2() testpackage.ok.Test1.getStr()
	 CALL testpackage.ok.Test2.test2() testpackage.ok.Test1.getStrStatic()
	 */
	public void test2() {
		((Test1) t1).getStr();
		((Test1) t1).getStrStatic();
	}
}
