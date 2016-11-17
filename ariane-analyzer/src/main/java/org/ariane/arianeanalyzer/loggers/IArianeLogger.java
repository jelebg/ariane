package org.ariane.arianeanalyzer.loggers;

public interface IArianeLogger {
	void init() throws Exception;
	void end() throws Exception;
	void logFileVisitBegin(String filename);
	void logFileVisitEnd(String filename);
	void logClassVisitBegin(String className);
	void logClassVisitEnd(String className);
	void logInheritence(String motherClassName, String childClassName) throws Exception;
	void logMethodCall(String callerQualifiedSignature, String calleeQualifiedSignature) throws Exception;
}
