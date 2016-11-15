package org.ariane.arianeanalyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.ariane.arianeanalyzer.loggers.IArianeLogger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.declarations.MethodDeclaration;
import com.github.javaparser.symbolsolver.model.declarations.TypeDeclaration;
import com.github.javaparser.symbolsolver.model.declarations.ValueDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public class LoggerVisitor extends VoidVisitorAdapter<VisitorContext> {
	IArianeLogger arianeLogger;
	TypeSolver typeSolver;
	JavaParserFacade jpf;
	
	public LoggerVisitor(IArianeLogger arianeLogger, TypeSolver typeSolver) {
		this.arianeLogger = arianeLogger;
		this.typeSolver = typeSolver;
		this.jpf = JavaParserFacade.get(typeSolver);
	}
	
	public void visitFile(String filename) throws FileNotFoundException {
		arianeLogger.logFileVisitBegin(filename);
        CompilationUnit cu;
        try(FileInputStream in = new FileInputStream(filename)) {
			cu = JavaParser.parse(in);
		
			VisitorContext ctx = new VisitorContext(cu, filename);
			visit(cu, ctx);
			
	    } catch (Exception e) {
	    	System.err.println("Exception while visiting file : " + filename);
			e.printStackTrace();
        } 
		arianeLogger.logFileVisitEnd(filename);
	}
	
	public String getCompleteClassNameFromPackage(VisitorContext ctx, String className) {
		String packageName = "";
		if(ctx.compilationUnit.getPackage().isPresent()) {
			packageName = ctx.compilationUnit.getPackage().get().getPackageName();
		}
		return packageName+"."+className;
	}
	
	public String getCompleteClassNameFromImports(VisitorContext ctx, String className) {
		// TODO : gerer sous classe + classe anonymes
		if(className.indexOf('.')>=0) {
			return className;
		}
		String packageName = ctx.imports.get(className);
		if(packageName == null) {
			return className;
		}
		return packageName+"."+className;
	}
	
	public String getMethodQualifiedName(VisitorContext ctx, com.github.javaparser.ast.body.MethodDeclaration methodDeclaration) {
		// TODO : subclasses + classes anonymes
		// TODO : generics
		StringBuilder sb = new StringBuilder();
		sb.append(ctx.getcurrentClassName());
		sb.append(".");
		sb.append(methodDeclaration.getName());
		sb.append("(");
		if(methodDeclaration.getParameters() != null) {
			boolean first = true;
			for(Parameter parameter : methodDeclaration.getParameters()) {
				if(!first) {
					sb.append(",");
				}
				first = false;
				String typeName = parameter.getType().toString();
				// TODO : gerer les pacakges java.lang.*, javax.* ???
				String fullTypeName = getCompleteClassNameFromImports(ctx, typeName);
				sb.append(fullTypeName);
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public void visit(ClassOrInterfaceDeclaration n, VisitorContext ctx) {
		String className = getCompleteClassNameFromPackage(ctx, n.getName());
		arianeLogger.logClassVisitBegin(className);
		ctx.pushClassName(className);
		try {

			if(n.getImplements() != null) {
				for(ClassOrInterfaceType implemented : n.getImplements()) {
					String implementedName = getCompleteClassNameFromImports(ctx, implemented.getName());
					arianeLogger.logInheritence(implementedName, className);
				}
			}
			if(n.getExtends() != null) {
				for(ClassOrInterfaceType extended : n.getExtends()) {
					String extendededName = getCompleteClassNameFromImports(ctx, extended.getName());
					arianeLogger.logInheritence(extendededName, className);
				}
			}
	
			super.visit(n, ctx);
			arianeLogger.logClassVisitEnd(className);
		}
		finally {
			ctx.popClassName();
		}

	}
	
	@Override
	public void visit(com.github.javaparser.ast.body.MethodDeclaration n, VisitorContext ctx) {
		ctx.currentMethodQualifiedName = getMethodQualifiedName(ctx, n);

		try  {
			super.visit(n, ctx);
		}
		finally {
			ctx.currentMethodQualifiedName = null;
		}
	}
	
	@Override
	public void visit(MethodCallExpr n, VisitorContext ctx) {
		super.visit(n, ctx);
		try {
			SymbolReference<MethodDeclaration> s = jpf.solve(n);
			if(! s.isSolved()) {
				System.err.println("ERROR : not solved by jpf.solve(n)");
			}
			else {
				String calleeQualifiedSignature = s.getCorrespondingDeclaration().getQualifiedSignature();
				arianeLogger.logMethodCall(ctx.currentMethodQualifiedName, calleeQualifiedSignature);
			}
		}
		catch(Exception e) {
	    	System.err.println("Exception while resovling MethodCallExpr:"+n.toString()+" in file: " + 
	    					ctx.filename+"["+n.getBegin().line+"]");
			e.printStackTrace();
		}
		
	}

}
