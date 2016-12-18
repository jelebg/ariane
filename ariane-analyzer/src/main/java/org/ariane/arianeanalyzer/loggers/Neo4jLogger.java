package org.ariane.arianeanalyzer.loggers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.google.common.collect.Maps;

import org.neo4j.driver.v1.Record;

public class Neo4jLogger implements IArianeLogger {
	// TODO : move in abstract utilitary class sowewhere
	public static final String TYPE_CLASS = "CLASS";
	public static final String TYPE_METHOD = "METHOD";
	public static final String TYPE_CALL = "CALL";
	public static final String TYPE_CALLBACK = "CALLBACK";
	public static final String TYPE_INHERITENCE = "INHERITENCE";
	public static final String TYPE_METHOD_OF_CLASS = "METHOD_OF_CLASS";
	
	
	private String hostname;
	private String port;
	private String user;
	private String pwd;
	
	private Driver driver;
	private Session session;
	

	public static void main(String [] argv) {
		new Neo4jLogger("localhost", "7687", "neo4j", "bonnevaux");
	}
	
	public Neo4jLogger(String hostname, String port, String user, String pwd) {
		this.hostname = hostname;
		this.port = port;
		this.user = user;
		this.pwd = pwd;

	}
	
	private void addNodeIfNotExists(String type, String name) {
		Map<String, Object> params = new HashMap<>();
		params.put("pName", name);
		session.run( "MERGE (n:"+type+" {name:{pName}})",  params);
	}

	private void addRelation(String relationType, String nodeFromType, String nodeToType, String nodeFrom, String nodeTo, boolean createIfNotExists) throws UnsupportedEncodingException, IOException {
		Map<String, Object> params = new HashMap<>();
		params.put("pNodeFrom", nodeFrom);
		params.put("pNodeTo", nodeTo);
		session.run(
				"MATCH (from:"+nodeFromType+"{name:{pNodeFrom}}) , (to:"+nodeToType+"{name:{pNodeTo}}) "
				+(createIfNotExists?"MERGE":"CREATE")+" (from)-[:"+relationType+"]->(to)",  params);
		
	}

	@Override
	public void init() {

		driver = GraphDatabase.driver( "bolt://"+hostname+":"+port, AuthTokens.basic( user, pwd ) );
		session = driver.session();
		
		session.run( "CREATE INDEX ON :"+TYPE_CLASS+"(name)");
		session.run( "CREATE INDEX ON :"+TYPE_METHOD+"(name)");

	}
	
	@Override
	public void end() throws IOException {
		if(session != null) {
			session.close();
			session = null;
		}
		if(driver != null) {
			driver.close();
			driver = null;
		}

	}
	
	@Override
	public void logFileVisitBegin(String filename) {
		// nothing
	}

	@Override
	public void logFileVisitEnd(String filename) {
		// nothing
	}

	@Override
	public void logClassVisitBegin(String className) {
		// nothing
	}

	@Override
	public void logClassVisitEnd(String className) {
		// nothing
	}

	@Override
	public void logInheritence(String motherClassName, String childClassName) throws UnsupportedEncodingException, IOException {
		addNodeIfNotExists(TYPE_CLASS, motherClassName);
		addNodeIfNotExists(TYPE_CLASS, childClassName);
		addRelation(TYPE_INHERITENCE, TYPE_CLASS, TYPE_CLASS, childClassName, motherClassName, true);
	}

	@Override
	public void logMethodCall(String callerQualifiedSignature, String calleeQualifiedSignature) throws UnsupportedEncodingException, IOException {
		callerQualifiedSignature = removeWhiteSpaces(callerQualifiedSignature);
		calleeQualifiedSignature = removeWhiteSpaces(calleeQualifiedSignature);
		
		addNodeIfNotExists(TYPE_METHOD, callerQualifiedSignature);
		addNodeIfNotExists(TYPE_METHOD, calleeQualifiedSignature);
		addRelation(TYPE_CALL, TYPE_METHOD, TYPE_METHOD, callerQualifiedSignature, calleeQualifiedSignature, false);
		
		String callerClass = getClassNameFromMethod(callerQualifiedSignature);
		String calleeClass = getClassNameFromMethod(calleeQualifiedSignature);
		addNodeIfNotExists(TYPE_CLASS, callerClass);
		addNodeIfNotExists(TYPE_CLASS, calleeClass);
		addRelation(TYPE_METHOD_OF_CLASS, TYPE_METHOD, TYPE_CLASS, callerQualifiedSignature, callerClass, true);
		addRelation(TYPE_METHOD_OF_CLASS, TYPE_METHOD, TYPE_CLASS, calleeQualifiedSignature, calleeClass, true);
	}
	
	@Override
	public void logCallback(String callerQualifiedSignature, String calleeQualifiedSignature) throws UnsupportedEncodingException, IOException {
		callerQualifiedSignature = removeWhiteSpaces(callerQualifiedSignature);
		calleeQualifiedSignature = removeWhiteSpaces(calleeQualifiedSignature);
		
		addNodeIfNotExists(TYPE_METHOD, callerQualifiedSignature);
		addNodeIfNotExists(TYPE_METHOD, calleeQualifiedSignature);
		addRelation(TYPE_CALLBACK, TYPE_METHOD, TYPE_METHOD, callerQualifiedSignature, calleeQualifiedSignature, false);
		
		String callerClass = getClassNameFromMethod(callerQualifiedSignature);
		String calleeClass = getClassNameFromMethod(calleeQualifiedSignature);
		addNodeIfNotExists(TYPE_CLASS, callerClass);
		addNodeIfNotExists(TYPE_CLASS, calleeClass);
		addRelation(TYPE_METHOD_OF_CLASS, TYPE_METHOD, TYPE_CLASS, callerQualifiedSignature, callerClass, true);
		addRelation(TYPE_METHOD_OF_CLASS, TYPE_METHOD, TYPE_CLASS, calleeQualifiedSignature, calleeClass, true);
	}
	
	// TODO : move in abstract utilitary class sowewhere
	private static String getClassNameFromMethod(String method) {
		int i = method.indexOf("(");
		if(i <= 0 ) {
			return method;
		}
		method = method.substring(0,  i);
		i = method.lastIndexOf(".");
		if(i <= 0 ) {
			return method;
		}
		return method.substring(0,  i);

	}

	// TODO : move in abstract utilitary class sowewhere
	private static String removeWhiteSpaces(String str) {
		return str.replaceAll("\\s", "");
	}

}
