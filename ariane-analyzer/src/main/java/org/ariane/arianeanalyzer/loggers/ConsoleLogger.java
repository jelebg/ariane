package org.ariane.arianeanalyzer.loggers;

public class ConsoleLogger implements IArianeLogger {

	@Override
	public void logFileVisitBegin(String filename) {
		System.out.println("FILE "+filename);
	}

	@Override
	public void logFileVisitEnd(String filename) {
		// nothing
	}

	@Override
	public void logClassVisitBegin(String className) {
		System.out.println("CLASS "+className);
	}

	@Override
	public void logClassVisitEnd(String className) {
		// nothing
	}

	@Override
	public void logInheritence(String motherClassName, String childClassName) {
		System.out.println("INHERITENCE "+motherClassName+" "+childClassName);
		
	}

	@Override
	public void logMethodCall(String callerQualifiedSignature, String calleeQualifiedSignature) {
		System.out.println("CALL "+callerQualifiedSignature+" "+calleeQualifiedSignature);
		
	}

	@Override
	public void init() throws Exception {
		// nothing
		
	}

	@Override
	public void end() throws Exception {
		// nothing
		
	}


}
