package org.ariane.arianeanalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.ariane.arianeanalyzer.loggers.ConsoleLogger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.declarations.MethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JreTypeSolver;

/*import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.model.declarations.MethodDeclaration;
import me.tomassetti.symbolsolver.model.resolution.SymbolReference;
import me.tomassetti.symbolsolver.model.resolution.TypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JarTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JreTypeSolver;*/


/**
 * Hello world!
 *
 */
public class ArianeAnalyzerMain 
{
	public static String targetDirectory = null;
	public static List<String> skipDirectories = null;

	public static void main( String[] args ) throws IOException {
		targetDirectory = args[0];
		if(args.length > 1) {
			skipDirectories = Arrays.asList(args[1].split(","));
		}
		System.out.println("visite de "+targetDirectory);
		System.out.println("en excluant "+skipDirectories);

		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
	    combinedTypeSolver.add(new JreTypeSolver());
	    combinedTypeSolver.add(new JavaParserTypeSolver(new File("C:\\DEV\\javaparser\\arbo.neon\\javaparser\\src\\main\\java")));
	    combinedTypeSolver.add(new JarTypeSolver("C:/Users/jean/.m2/repository/com/github/javaparser/java-symbol-solver-core/0.3.2/java-symbol-solver-core-0.3.2.jar"));
	    combinedTypeSolver.add(new JarTypeSolver("C:/Users/jean/.m2/repository/com/github/javaparser/javaparser-core/3.0.0-alpha.6/javaparser-core-3.0.0-alpha.6.jar"));
		
		
		File f = new File(targetDirectory);
		if(f.isDirectory()) {
			visitDirectory(targetDirectory, combinedTypeSolver);
		}
		else {
			visitFile(targetDirectory, combinedTypeSolver);
		}

    }
	
	public static void visitFile(String filename, final TypeSolver typeSolver) throws IOException {
        try {
        	final JavaParserFacade jpf = JavaParserFacade.get(typeSolver);
			ConsoleLogger logger = new ConsoleLogger();
			LoggerVisitor visitor = new LoggerVisitor(logger, typeSolver);
			visitor.visitFile(filename);
			
	    } catch (Exception e) {
	    	System.err.println("Exception while visiting file : " + filename);
			e.printStackTrace();
        } 
	}
	
	public static void visitDirectory(String dir, final TypeSolver typeSolver) throws IOException {
		
		Files.walkFileTree(Paths.get(dir), 
				new FileVisitor<Path>() {

					@Override
					public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						if(skipDirectories!=null && skipDirectories.contains(dir.getFileName().toString())) {
							return FileVisitResult.SKIP_SUBTREE;
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if(!file.getFileName().toString().endsWith(".java")) {
							return FileVisitResult.CONTINUE;
						}
						
						System.out.println("FILE "+file.toString());
						ArianeAnalyzerMain.visitFile(file.toString(), typeSolver);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				}
		);

		
	}
}
