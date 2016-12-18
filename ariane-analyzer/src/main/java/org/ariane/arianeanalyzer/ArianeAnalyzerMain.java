package org.ariane.arianeanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.ariane.arianeanalyzer.loggers.CsvLogger;
import org.ariane.arianeanalyzer.loggers.IArianeLogger;
import org.ariane.arianeanalyzer.loggers.Neo4jLogger;

import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JreTypeSolver;

public class ArianeAnalyzerMain 
{
	private static List<String> skipDirectories = null;
	private static CombinedTypeSolver typeSolver = null;
	
	public static void main( String[] args ) throws Exception {
	
		Options options = new Options();
		
		Option jarFileOrDir = new Option("j", "jar", true, "jar file(s) or directory(ies) containing jars");
		jarFileOrDir.setRequired(true);
		options.addOption(jarFileOrDir);
		
		Option sourceDir = new Option("s", "src", true, "source directory(ies)");
		sourceDir.setRequired(true);
		options.addOption(sourceDir);
		
		Option visitDir = new Option("v", "visit", true, "visit directory(ies)");
		visitDir.setRequired(false);
		options.addOption(visitDir);
		
		Option skipName = new Option("k", "skip", true, "skip name(s)");
		skipName.setRequired(false);
		options.addOption(skipName);
		
		Option outputName = new Option("o", "output", true, "ouput connectors : stdout(default), unitTesting, csv, neo4j");
		outputName.setRequired(false);
		options.addOption(outputName);
		
		Option outputInfosName = new Option("i", "outputInformations", true, "csv output nodes and relations files prefixes");
		outputInfosName.setRequired(false);
		options.addOption(outputInfosName);
		
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
		String[] srcs     = cmd.getOptionValues("src");
		String[] visits   = cmd.getOptionValues("visit");
		String[] skips    = cmd.getOptionValues("skip");
		
		String output = cmd.getOptionValue("output", "stdout");
		String outputInformations = cmd.getOptionValue("outputInformations");
		boolean unitTesting = "unitTesting".equals(output);
		
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
		
		for(String src : srcs) {
			System.out.println("src:"+src);
			typeSolver.add(new JavaParserTypeSolver(new File(src)));
		}
		if(visits == null) {
			visits = srcs;
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
		
		IArianeLogger logger = createLogger(output, outputInformations);
		logger.init();
		for(String visit : visits) {
			File f = new File(visit);
			if(f.isDirectory()) {
				visitDirectory(visit, logger, unitTesting);
			}
			else {
				visitFile(visit, logger, unitTesting);
			}
		}
		logger.end();

    }
	
	public static IArianeLogger createLogger(String output, String outputInformations) throws FileNotFoundException {
		switch(output) {
		// TODO : constantes
		case "stdout" :
		case "unitTesting" :		
			return new ConsoleLogger();
		
		case "csv" :
			File nodesFile = new File(outputInformations+".nodes.csv");
			File relationsFile = new File(outputInformations+".relations.csv");
			return new CsvLogger(nodesFile, relationsFile);
			
		case "neo4j" :
			String[] splt = outputInformations.split(":");
			return new Neo4jLogger(splt[0], splt[1], splt[2], splt[3]);
			
		default :
			throw new RuntimeException("wrong output format : "+output);
		}
		
	}
	
	public static void visitFile(String filename, IArianeLogger logger, boolean unitTesting) throws IOException {
        try {
        	final JavaParserFacade jpf = JavaParserFacade.get(typeSolver);
			LoggerVisitor visitor = unitTesting ? new UnitTestVisitor((ConsoleLogger) logger, typeSolver, false) : new LoggerVisitor(logger, typeSolver);
			visitor.visitFile(filename);

			
	    } catch (Exception e) {
	    	System.err.println("Exception while visiting file : " + filename);
			e.printStackTrace();
        } 
	}
	
	public static void visitDirectory(String dir, IArianeLogger logger, boolean unitTesting) throws IOException {
		
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
						
						ArianeAnalyzerMain.visitFile(file.toString(), logger, unitTesting);
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
