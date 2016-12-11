package org.ariane.arianeanalyzer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.ariane.arianeanalyzer.loggers.ConsoleLogger;

import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public class UnitTestVisitor extends LoggerVisitor {

	TestStream testStream;
	boolean verbose;
	
	public UnitTestVisitor(ConsoleLogger consoleLogger, TypeSolver typeSolver, boolean verbose) {
		super(consoleLogger, typeSolver);
		this.verbose = verbose;
		testStream = new TestStream(verbose);
		((ConsoleLogger) this.arianeLogger).setOutStream(testStream);
	}
	
	public class TestStream extends PrintStream {
		// use a list to maintain order and allow for duplicates. will be less efficient than a HashSet, though
		ArrayList<String> linesToHave = new ArrayList<String>();
		boolean verbose;
		
		public TestStream(boolean verbose) {
			super(System.out);
			this.verbose = verbose;
		}
		
		public void addLinesToHave(String [] lines, int fromIndex) {
			for(int i=fromIndex; i<lines.length; i++) {
				String trimed =  lines[i].trim();
				if(trimed!=null && !trimed.isEmpty()) {
					linesToHave.add(trimed);
				}
			}
		}
		
		@Override
		public void println(String str) {
			String trimed = str.trim();
			boolean removed = linesToHave.remove(trimed);
			// test only if a CALL is unexpected. for others, do not care
			if (!removed && (trimed.startsWith("CALL ") || trimed.startsWith("CALLBACK "))) {
				System.out.println("!!! UNEXPECTED : "+str);
			}
			if(verbose) {
				super.println(str);
			}
		}
		
		public void checkAndReset() {
			for(String line : linesToHave) {
				super.println("!!! MISSING : "+line);
			}
			
			linesToHave.clear();
		}
		
	}
	
	@Override
	public void visit(BlockComment blockComment, VisitorContext ctx) {
		super.visit(blockComment, ctx);
		String [] lines = blockComment.getContent().split("\n");
		if ("-Ariane".equals(lines[0].trim())) {
			testStream.checkAndReset();
			testStream.addLinesToHave(lines, 1);
		}
	}

	@Override
	public void visitFile(String filename) throws Exception {
		try {
			super.visitFile(filename);
		}
		finally {
			testStream.checkAndReset();
		}

	}
}
