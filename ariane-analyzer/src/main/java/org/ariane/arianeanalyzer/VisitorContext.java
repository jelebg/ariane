package org.ariane.arianeanalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.imports.ImportDeclaration;
import com.github.javaparser.ast.imports.SingleTypeImportDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class VisitorContext {
	public String filename;
	public CompilationUnit compilationUnit;
	Map<String, String> imports;
	List<String> classNameStack;
	String currentMethodQualifiedName;

	public VisitorContext(CompilationUnit compilationUnit, String filename) {
		this.compilationUnit = compilationUnit;
		this.filename = filename;
		
		classNameStack = new ArrayList<>();
		imports = new HashMap<>();
		if(compilationUnit.getImports() != null) {
			for(ImportDeclaration importDeclaration : compilationUnit.getImports()) {
				SingleTypeImportDeclaration stid = (SingleTypeImportDeclaration) importDeclaration;
				String importName = stid.getType().getName();
				int i = importName.lastIndexOf(".");
				if(i<0) {
					imports.put(importName, importName);
				}
				else {
					String className = importName.substring(i+1);
					String packageName = importName.substring(0,i);
					imports.put(className, packageName);
				}
			}
		}
	}

	public void pushClassName(String className) {
		classNameStack.add(className);
	}
	public void popClassName() {
		classNameStack.remove(classNameStack.size()-1);
	}
	public String getcurrentClassName() {
		return classNameStack.get(classNameStack.size()-1);
	}
}
