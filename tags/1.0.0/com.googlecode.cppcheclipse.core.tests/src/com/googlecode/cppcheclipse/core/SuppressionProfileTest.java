package com.googlecode.cppcheclipse.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.cppcheclipse.tests.MockPreferenceStore;

public class SuppressionProfileTest {

	private SuppressionProfile profile;
	
	@Before
	public void setup() {
		profile = new SuppressionProfile(new MockPreferenceStore(), null);
	}
	
	@Test
	public void testFileSuppression() {
		File file = new File("/test/testfile");
		profile.addFileSuppression(file);
		assertTrue(profile.isFileSuppressed(file));
	}
	
	@Test
	public void testFolderSuppression() {
		File file = new File("/test/testfolder");
		profile.addFileSuppression(file);
		assertTrue(profile.isFileSuppressed(new File("/test/testfolder/testfile")));
		profile.addFileSuppression(file);
		assertFalse(profile.isFileSuppressed(new File("/test/testfolder2/testfile")));
	}
	
	@Test
	public void testLineSuppression() {
		File file = new File("/test/testfile");
		profile.addProblemInLineSuppression(file, "id1", 12);
		assertTrue(profile.isProblemInLineSuppressed(file, "id1", 12));
	}
	
	@Test
	public void testProblemSuppression() {
		File file = new File("/test/testfile");
		profile.addProblemSuppression(file, "id1");
		assertTrue(profile.isProblemInLineSuppressed(file, "id1", 12));
	}
	
	@Test
	public void testProblemSuppressionForSuppressedFile() {
		File file = new File("/test/testfolder");
		profile.addFileSuppression(file);
		assertTrue(profile.isProblemInLineSuppressed(new File("/test/testfolder"), "id1", 12));
	}
}
