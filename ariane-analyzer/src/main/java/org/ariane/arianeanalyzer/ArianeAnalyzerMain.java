package org.ariane.arianeanalyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
	private static List<String> skipDirectories = null;
	private static CombinedTypeSolver typeSolver = null;
	
	public static void main( String[] args ) throws IOException {
	
		Options options = new Options();
		
		Option jarFileOrDir = new Option("j", "jar", true, "jar file(s) or directory(ies) containing jars");
		jarFileOrDir.setRequired(true);
		options.addOption(jarFileOrDir);
		
		Option sourceDir = new Option("s", "src", true, "source directory(ies)");
		sourceDir.setRequired(true);
		options.addOption(sourceDir);
		
		Option skipName = new Option("k", "skip", true, "skip name(s)");
		skipName.setRequired(false);
		options.addOption(skipName);
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;
		
		try {
		    cmd = parser.parse(options, args);
		} catch (ParseException e) {
		    System.out.println(e.getMessage());
		    formatter.printHelp("utility-name", options);
		
		    System.exit(1);
		    return;
		}
		
		String[] jarDirs  = cmd.getOptionValues("jar");
		String[] srcs  = cmd.getOptionValues("src");
		String[] skips = cmd.getOptionValues("skip");
		List<String> jars = new ArrayList<String>();
		
		for(String str : jarDirs) {
			File f = new File(str);
			if(f.isDirectory()) {
		        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(str), "*.jar")) {
		            for (Path path : directoryStream) {
		                jars.add(path.toString());
		            }
		        } catch (IOException ex) {
		        	ex.printStackTrace();
		        }

			}
			else if(f.isFile()) {
				jars.add(str);
			}
			
		}
	        

		typeSolver = new CombinedTypeSolver();
		typeSolver.add(new JreTypeSolver());
	    /*
	    typeSolver.add(new JavaParserTypeSolver(new File("C:\\DEV\\javaparser\\arbo.neon\\javaparser\\src\\main\\java")));
	    typeSolver.add(new JarTypeSolver("C:/Users/jean/.m2/repository/com/github/javaparser/java-symbol-solver-core/0.3.2/java-symbol-solver-core-0.3.2.jar"));
	    typeSolver.add(new JarTypeSolver("C:/Users/jean/.m2/repository/com/github/javaparser/javaparser-core/3.0.0-alpha.6/javaparser-core-3.0.0-alpha.6.jar"));
	    */
		
		for(String src : srcs) {
			System.out.println("src:"+src);
			typeSolver.add(new JavaParserTypeSolver(new File(src)));
		}
		for(String jar : jars) {
			System.out.println("jar:"+jar);
			typeSolver.add(new JarTypeSolver(jar));
		}
		if(skips != null) {
			for(String str : skips) {
				System.out.println("skip:"+str);
			}
			skipDirectories = Arrays.asList(skips);
		}
		
		for(String src : srcs) {
			File f = new File(src);
			if(f.isDirectory()) {
				visitDirectory(src);
			}
			else {
				visitFile(src);
			}
		}

    }
	
	public static void visitFile(String filename) throws IOException {
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
	
	public static void visitDirectory(String dir) throws IOException {
		
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
						ArianeAnalyzerMain.visitFile(file.toString());
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
