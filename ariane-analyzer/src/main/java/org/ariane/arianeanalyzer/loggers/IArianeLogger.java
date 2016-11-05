package org.ariane.arianeanalyzer.loggers;

public interface IArianeLogger {
	void logFileVisitBegin(String filename);
	void logFileVisitEnd(String filename);
	void logClassVisitBegin(String className);
	void logClassVisitEnd(String className);
	void logInheritence(String motherClassName, String childClassName);
	void logMethodCall(String callerQualifiedSignature, String calleeQualifiedSignature);
}
