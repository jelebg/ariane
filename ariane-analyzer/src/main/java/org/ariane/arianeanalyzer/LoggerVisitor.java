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
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.declarations.MethodDeclaration;
import com.github.javaparser.symbolsolver.model.declarations.TypeDeclaration;
import com.github.javaparser.symbolsolver.model.declarations.ValueDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.ArrayType;
import com.github.javaparser.symbolsolver.model.typesystem.PrimitiveType;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;
import com.github.javaparser.symbolsolver.model.typesystem.Type;

public class LoggerVisitor extends VoidVisitorAdapter<VisitorContext> {
	IArianeLogger arianeLogger;
	TypeSolver typeSolver;
	JavaParserFacade jpf;
	
	public LoggerVisitor(IArianeLogger arianeLogger, TypeSolver typeSolver) {
		this.arianeLogger = arianeLogger;
		this.typeSolver = typeSolver;
		this.jpf = JavaParserFacade.get(typeSolver);
	}
	
	public void visitFile(String filename) throws Exception {
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
	
	/*public String getCompleteClassNameFromType(Type t) {
		//TODO : gerer les generics
		String name = null;
		if(t.isArray()) {
			ArrayType arrayType = t.asArrayType();
			Type t2 = arrayType.getComponentType();
			name = getCompleteClassNameFromType(t2)+"[]";
		}
		else if(t.isNull()) {
			throw new UnsupportedOperationException("getCompleteClassNameFromType for null");
		}
		else if(t.isPrimitive()) {
			//PrimitiveType primitiveType = t.asPrimitive();
			throw new UnsupportedOperationException("getCompleteClassNameFromType for PrimitiveType");
		}
		else if(t.isReference()) {
			ReferenceType referenceType = t.asReferenceType();
			referenceType.
		}
		else if(t.isReferenceType()) {
		}
		else if(t.isTypeVariable()) {
			throw new UnsupportedOperationException("getCompleteClassNameFromType for TypeParameter");
		}
		else if(t.isVoid()) {
			throw new UnsupportedOperationException("getCompleteClassNameFromType for void");
		}
		else if(t.isWildcard())
			throw new UnsupportedOperationException("getCompleteClassNameFromType for Wildcard");
		}
		
	}*/

	
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
		sb.append(ctx.getcurrentClass().name);
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
		ctx.pushClass(className);
		try {

			if(n.getImplements() != null) {
				for(ClassOrInterfaceType implemented : n.getImplements()) {
					String implementedName = getCompleteClassNameFromImports(ctx, implemented.getName());
					try {
						arianeLogger.logInheritence(implementedName, className);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if(n.getExtends() != null) {
				for(ClassOrInterfaceType extended : n.getExtends()) {
					String extendededName = getCompleteClassNameFromImports(ctx, extended.getName());
					try {
						arianeLogger.logInheritence(extendededName, className);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
	
			super.visit(n, ctx);
			arianeLogger.logClassVisitEnd(className);
		}
		finally {
			ctx.popClass();
		}

	}
	
	@Override
	public void visit(ObjectCreationExpr n, VisitorContext ctx) {
		Type t = jpf.getType(n);
		String className = t.describe();
		
		ctx.pushClass(className);
		
		try  {
			super.visit(n, ctx);
		}
		finally {
			ctx.popClass();
		}
	}
	
	@Override
	public void visit(com.github.javaparser.ast.body.MethodDeclaration n, VisitorContext ctx) {
		ctx.getcurrentClass().currentMethodQualifiedName = getMethodQualifiedName(ctx, n);

		try  {
			super.visit(n, ctx);
		}
		finally {
			ctx.getcurrentClass().currentMethodQualifiedName = null;
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
				arianeLogger.logMethodCall(ctx.getcurrentClass().currentMethodQualifiedName, calleeQualifiedSignature);
			}
		}
		catch(Exception e) {
	    	System.err.println("Exception while resovling MethodCallExpr:"+n.toString()+" in file: " + 
	    					ctx.filename+"["+n.getBegin().line+"]");
			e.printStackTrace();
		}
		
	}

}
