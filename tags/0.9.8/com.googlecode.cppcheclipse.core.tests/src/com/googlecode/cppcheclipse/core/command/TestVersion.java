package com.googlecode.cppcheclipse.core.command;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestVersion {

	// compare with issue 47
	@Test
	public void testDevelopmentVersion() {
		String versionString = "cppcheck 1.55 dev";
		Version version = new Version(versionString);
		assertEquals(1, version.getMajorVersion());
		assertEquals(55, version.getMinorVersion());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidVersion() {
		new Version("1.2.3");
	}
	
	@Test
	public void testReleaseVersion() {
		String versionString = "cppcheck 1.55";
		Version version = new Version(versionString);
		assertEquals(1, version.getMajorVersion());
		assertEquals(55, version.getMinorVersion());
	}
	
	@Test
	public void testReleaseVersionWithRevision() {
		String versionString = "cppcheck 1.2.3";
		Version version = new Version(versionString);
		assertEquals(1, version.getMajorVersion());
		assertEquals(2, version.getMinorVersion());
		assertEquals(3, version.getRevision());
	}
}
