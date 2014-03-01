package com.googlecode.cppcheclipse.core.command;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.cppcheclipse.core.Problem;

public class TestCppcheckCommand {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseResultLines() throws Exception {
		/**
		 * line should have the following format
		 * <file>;<line>;<severity>;<id>;<message>
		 */
		
		// we must use absolute file names here, otherwise NPE due to project = null
		String line1 = "/var/tmp/file1.cpp;112;error;08;This is a test message; with a semicolon";
		String line2 = "/var/tmp/file2.cpp;7;error;112;Error message2";
		Problem problem1 = CppcheckCommand.parseResult(line1, null);
		
		assertProblem(problem1, new File("/var/tmp/file1.cpp"), 112, "error", "08", "This is a test message; with a semicolon" );
		
		Problem problem2 = CppcheckCommand.parseResult(line2, null);
		assertProblem(problem2, new File("/var/tmp/file2.cpp"), 7, "error", "112", "Error message2");
		
		String line3 = ";;information;missingInclude;Cppcheck cannot find all the include files (use --check-config for details)";
		Problem problem3 = CppcheckCommand.parseResult(line3, null);
		assertProblem(problem3, null, -1, "information", "missingInclude", "Cppcheck cannot find all the include files (use --check-config for details)");
	}
	
	private void assertProblem(Problem problem, File file, int line,  String category, String id,  String message) {
		assertEquals(id, problem.getId());
		assertEquals(file, problem.getFile());
		assertEquals(line, problem.getLineNumber());
		assertEquals(category, problem.getCategory());
		assertEquals(id, problem.getId());
		assertEquals(message, problem.getMessage());
	}
	
	@Test
	public void testParseFilename() throws Exception {
		String line = "Checking src/bug 10.cpp...";
		String filename = CppcheckCommand.parseFilename(line);
		
		assertEquals("src/bug 10.cpp", filename);
	}
	
	@Test
	public void testParseProgress() throws Exception {
		String line = "4/120 files checked 100% done";
		Integer fileNumber = CppcheckCommand.parseProgress(line);
		
		assertEquals(new Integer(4), fileNumber);
	}
}
