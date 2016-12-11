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
	List<ClassContext> classStack;

	public class ClassContext {
		String name;
		String currentMethodQualifiedName;
		boolean isAnonymous;
		
		public ClassContext(String name, String currentMethodQualifiedName, boolean isAnonymous) {
			this.name = name;
			this.currentMethodQualifiedName = currentMethodQualifiedName;
			this.isAnonymous = isAnonymous;
		}
		
	}
	
	public VisitorContext(CompilationUnit compilationUnit, String filename) {
		this.compilationUnit = compilationUnit;
		this.filename = filename;
		
		classStack = new ArrayList<>();
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

	public void pushClass(String className, boolean anonymousClass) {
		classStack.add(new ClassContext(className, null, anonymousClass));
	}
	public void popClass() {
		classStack.remove(classStack.size()-1);
	}
	public ClassContext getCurrentClass() {
		return classStack.get(classStack.size()-1);
	}
	
	public String getCompilationUnitPackage() {
		if(compilationUnit.getPackage().isPresent()) {
			return compilationUnit.getPackage().get().getPackageName();
		}
		return "";
	}
	
	public String getCurrentMethodQualidName() {
		// on peut initialiser un contexte de class pour son instantiation alors qu'on est pas encore dans un méthode
		// 1 seul cas à gérer : parametre du constructeur (Test4)
		// les constructeurs statiques sont interdits dans les classes anonymes, donc ce cas n'est pas à gérer
		for(int i=classStack.size()-1; i>=0; i--) {
			ClassContext classContext = classStack.get(i);
			if(classContext.currentMethodQualifiedName != null) {
				return classContext.currentMethodQualifiedName;
			}
		}
		return null;
	}
	
	public String getUpperCurrentMethodQualidName() {
		// on peut initialiser un contexte de class pour son instantiation alors qu'on est pas encore dans un méthode
		// 1 seul cas à gérer : parametre du constructeur (Test4)
		// les constructeurs statiques sont interdits dans les classes anonymes, donc ce cas n'est pas à gérer
		for(int i=classStack.size()-2; i>=0; i--) {
			ClassContext classContext = classStack.get(i);
			if(classContext.currentMethodQualifiedName != null) {
				return classContext.currentMethodQualifiedName;
			}
		}
		return null;

	}
}
