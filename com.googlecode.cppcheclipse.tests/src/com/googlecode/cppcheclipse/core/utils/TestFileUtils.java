package com.googlecode.cppcheclipse.core.utils;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestFileUtils {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRelativize() throws Exception {
		File file1 = new File("/abc/def/e f");
		File file2 = new File("/abc/def/e f/g/h i");
		File result = FileUtils.relativizeFile(file1, file2);
		assertEquals("g/h i", result.toString());
	}
}
