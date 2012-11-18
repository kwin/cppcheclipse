package com.googlecode.cppcheclipse.core.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSerializeHelper {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSerializeHelper() throws Exception {
		File file1 = new File("/tmp/var/test");
		String serialized = SerializeHelper.toString(file1);
		File file2 = (File) SerializeHelper.fromString(serialized);
		assertEquals(file1, file2);
	}
}
