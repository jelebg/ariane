package testpackage.ok;


// inline class declaration
public class Test4 {

	public static final String str = "test";
	
	/*-Ariane
	CALLBACK testpackage.ok.Test4.test1() java.io.PrintStream.println(java.lang.String)
	CALL testpackage.ok.Test4.test1() testpackage.ok.Thread.Thread(java.lang.Runnable)
	CALL testpackage.ok.Test4.test1() testpackage.ok.Runnable.Runnable()
	CALL java.lang.Runnable.run() java.io.PrintStream.println(java.lang.String)
	 */
	void test1() {
		// TODO rajouter un type d'appel CALLABLE pour Test4 -> Sysem.out.println sans tenir compte de l'interface éphemere
		Thread t = new Thread(new Runnable()  {
			@Override
			public void run() {
				System.out.println(str);
			}
		});

	}
}
