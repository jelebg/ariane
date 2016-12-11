package org.ariane.arianeanalyzer.loggers;

import java.io.PrintStream;

public class ConsoleLogger implements IArianeLogger {
	
	PrintStream out;

	public ConsoleLogger() {
		this.out = System.out;
	}

	public ConsoleLogger(PrintStream out) {
		this.out = out;
	}
	
	public void setOutStream(PrintStream out) {
		this.out = out;
	}

	@Override
	public void logFileVisitBegin(String filename) {
		out.println("FILE "+filename);
	}

	@Override
	public void logFileVisitEnd(String filename) {
		// nothing
	}

	@Override
	public void logClassVisitBegin(String className) {
		out.println("CLASS "+className);
	}

	@Override
	public void logClassVisitEnd(String className) {
		// nothing
	}

	@Override
	public void logInheritence(String motherClassName, String childClassName) {
		out.println("INHERITENCE "+motherClassName+" "+childClassName);
		
	}

	@Override
	public void logMethodCall(String callerQualifiedSignature, String calleeQualifiedSignature) {
		out.println("CALL "+callerQualifiedSignature+" "+calleeQualifiedSignature);
		
	}
	
	@Override
	public void logCallback(String callerQualifiedSignature, String calleeQualifiedSignature) {
		out.println("CALLBACK "+callerQualifiedSignature+" "+calleeQualifiedSignature);
		
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
