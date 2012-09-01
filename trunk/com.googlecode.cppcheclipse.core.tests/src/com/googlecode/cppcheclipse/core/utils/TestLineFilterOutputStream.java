package com.googlecode.cppcheclipse.core.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestLineFilterOutputStream {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLineFilterOutputStream() throws Exception {
		String lines = "Line1\n";
		lines += "Line2";
		ByteArrayInputStream input = new ByteArrayInputStream(lines.getBytes("ASCII"));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		LineFilterOutputStream lfOutput = new LineFilterOutputStream(output, "ASCII");
		IOUtils.copy(input, lfOutput);
		
		assertEquals("Line1\n", output.toString("ASCII"));
		lfOutput.close();
		
		assertEquals(lines, output.toString("ASCII"));
	}
	
	/**
	 * Make sure that even unwanted line do not lead to invalid XML (due to bugs in the XML)
	 * @throws Exception
	 */
	@Test
	public void testLineFilterOutputStreamWithBlacklist() throws Exception {
		String lines = "Line1\n";
		lines += "Line2\n";
		lines += "cppcheck: No C or C++ source files found.\n";
		ByteArrayInputStream input = new ByteArrayInputStream(lines.getBytes("ASCII"));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		LineFilterOutputStream lfOutput = new LineFilterOutputStream(output, "ASCII");
		lfOutput.addBlacklistPattern("^Line2.*\\s*");
		lfOutput.addBlacklistPattern("^cppcheck: No C or C\\+\\+ source files found\\..*\\s*");
		IOUtils.copy(input, lfOutput);
		lfOutput.close();
		assertEquals("Line1\n", output.toString("ASCII"));
	}
}
