package org.ariane.arianeanalyzer.loggers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CsvLogger implements IArianeLogger {
	public static final String THE_CHARSET = "UTF-8";
	public static final String TYPE_CLASS = "CLASS";
	public static final String TYPE_METHOD = "METHOD";
	public static final String TYPE_CALL = "CALL";
	public static final String TYPE_CALLBACK = "CALLBACK";
	public static final String TYPE_INHERITENCE = "INHERITENCE";
	public static final String TYPE_METHOD_OF_CLASS = "METHOD_OF_CLASS";
	
	OutputStream nodesFile;
	OutputStream relationsFile;
	
	Map<String, Node> nodes = new HashMap<>();
	Set<Relation> relations = new HashSet<>();

	public class Relation {
		String type;
		String from;
		String to;

		public Relation() {
			
		}
		public Relation(String type, String from, String to) {
			this.type = type;
			this.from = from;
			this.to = to;
		}
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Relation other = (Relation) obj;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
		
	}
	
	public class Node {
		String type;
		String name;

		public Node() {
			
		}
		public Node(String type, String name) {
			this.type = type;
			this.name = name;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Node [type=" + type + ", name=" + name + "]";
		}
		
		
		
	}
	
	public CsvLogger(File nodesFiles, File relationsFile) throws FileNotFoundException {
		this.nodesFile = new BufferedOutputStream(new FileOutputStream(nodesFiles));
		this.relationsFile = new BufferedOutputStream(new FileOutputStream(relationsFile));
	}
	
	private void addNode(String type, String name) {
		Node newNode = new Node();
		newNode.name = name;
		newNode.type = type;
		
		Node n = nodes.putIfAbsent(name, newNode);
		
		if(n != null) {
			if(!n.equals(newNode)) {
				System.err.println("ERROR: conflict between different nodes with same name : "+newNode+" and "+n);
			}
		}
	}
	
	String getCsvString(String str) {
		if(str==null) {
			return "";
		}
		if(str.contains("\\") || str.contains(",")) {
			return "\""+str.replace("\"", "\\\"")+"\"";
		}
		
		return str;
	}
	
	private void printNode(Node n) throws UnsupportedEncodingException, IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(getCsvString(n.type));
		sb.append(",");
		sb.append(getCsvString(n.name));
		sb.append("\r\n");
		nodesFile.write(sb.toString().getBytes(THE_CHARSET));
	}
	
	private void printRelation(String type, String nodeFrom, String nodeTo) throws UnsupportedEncodingException, IOException {
		relations.add(new Relation(type, nodeFrom, nodeTo));
		StringBuilder sb = new StringBuilder();
		sb.append(getCsvString(type));
		sb.append(",");
		sb.append(getCsvString(nodeFrom));
		sb.append(",");
		sb.append(getCsvString(nodeTo));
		sb.append("\r\n");
		relationsFile.write(sb.toString().getBytes(THE_CHARSET));
	}
	private void printRelationIfNotAlreadyPrinted(String type, String nodeFrom, String nodeTo) throws UnsupportedEncodingException, IOException {
		if(relations.contains(new Relation(type, nodeFrom, nodeTo))) {
			return;
		}
		printRelation(type, nodeFrom, nodeTo);
	}

	@Override
	public void init() {

		try {
			printNode(new Node("TYPE", "NAME"));
			printRelation("TYPE", "NODE_FROM", "NODE_TO");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void end() throws IOException {
		for(Node n : nodes.values()) {
			try {
				printNode(n);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nodesFile.close();
		relationsFile.close();
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
		addNode(TYPE_CLASS, motherClassName);
		addNode(TYPE_CLASS, childClassName);
		printRelation(TYPE_INHERITENCE, childClassName, motherClassName);
	}

	@Override
	public void logMethodCall(String callerQualifiedSignature, String calleeQualifiedSignature) throws UnsupportedEncodingException, IOException {
		callerQualifiedSignature = removeWhiteSpaces(callerQualifiedSignature);
		calleeQualifiedSignature = removeWhiteSpaces(calleeQualifiedSignature);
		
		addNode(TYPE_METHOD, callerQualifiedSignature);
		addNode(TYPE_METHOD, calleeQualifiedSignature);
		printRelation(TYPE_CALL, callerQualifiedSignature, calleeQualifiedSignature);
		
		String callerClass = getClassNameFromMethod(callerQualifiedSignature);
		String calleeClass = getClassNameFromMethod(calleeQualifiedSignature);
		addNode(TYPE_CLASS, callerClass);
		addNode(TYPE_CLASS, calleeClass);
		printRelationIfNotAlreadyPrinted(TYPE_METHOD_OF_CLASS, callerQualifiedSignature, callerClass);
		printRelationIfNotAlreadyPrinted(TYPE_METHOD_OF_CLASS, calleeQualifiedSignature, calleeClass);
	}
	
	@Override
	public void logCallback(String callerQualifiedSignature, String calleeQualifiedSignature) throws UnsupportedEncodingException, IOException {
		callerQualifiedSignature = removeWhiteSpaces(callerQualifiedSignature);
		calleeQualifiedSignature = removeWhiteSpaces(calleeQualifiedSignature);
		
		addNode(TYPE_METHOD, callerQualifiedSignature);
		addNode(TYPE_METHOD, calleeQualifiedSignature);
		printRelation(TYPE_CALLBACK, callerQualifiedSignature, calleeQualifiedSignature);
		
		String callerClass = getClassNameFromMethod(callerQualifiedSignature);
		String calleeClass = getClassNameFromMethod(calleeQualifiedSignature);
		addNode(TYPE_CLASS, callerClass);
		addNode(TYPE_CLASS, calleeClass);
		printRelationIfNotAlreadyPrinted(TYPE_METHOD_OF_CLASS, callerQualifiedSignature, callerClass);
		printRelationIfNotAlreadyPrinted(TYPE_METHOD_OF_CLASS, calleeQualifiedSignature, calleeClass);
	}
	
	
	public static String getClassNameFromMethod(String method) {
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

	public static String removeWhiteSpaces(String str) {
		return str.replaceAll("\\s", "");
	}

}
