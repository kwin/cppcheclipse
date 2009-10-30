package com.googlecode.cppcheclipse.command;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Version {

	private static final String VERSION_PREFIX = "cppcheck";
	private static final String DELIMITER = ".";
	private final int majorVersion;
	private final int minorVersion;
	private final int revision;
	
	
	/** version string must have the format 
	 * "cppcheck <majorVersion>.<minorVersion>[.<revision>]"
	 * 
	 * @param version
	 */
	public Version(String version) {
		version = version.toLowerCase();
		
		if (!version.startsWith(VERSION_PREFIX)) {
			throw new IllegalArgumentException("Version must start with " + VERSION_PREFIX + ", but is " + version);
		}
		
		version = version.substring(VERSION_PREFIX.length());
		version = version.trim();
		
		try {
			StringTokenizer tokenizer = new StringTokenizer(version, DELIMITER);
			String versionPart = tokenizer.nextToken();
			majorVersion = Integer.parseInt(versionPart);
			versionPart = tokenizer.nextToken();
			minorVersion = Integer.parseInt(versionPart);
			
			if (tokenizer.hasMoreTokens()) {
				versionPart = tokenizer.nextToken();
				revision = Integer.parseInt(versionPart);
			} else {
				revision = 0;
			}
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Version must consist of at least two integers, separated by " + DELIMITER);
		}
	}



	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public int getRevision() {
		return revision;
	}
	
	/**
	 * 
	 * @param version
	 * @return true if the given version is greater than the current version
	 */
	public boolean isGreaterThan(Version version) {
		if (majorVersion > version.majorVersion)
			return true;
		else if (majorVersion == version.majorVersion) {
			if (minorVersion > version.minorVersion)
				return true;
			else if (minorVersion == version.minorVersion) {
				if (revision > version.revision)
					return true;
			}
		}
		return false;
	}



	@Override
	public String toString() {
		StringBuffer version = new StringBuffer();
		version.append(majorVersion).append(".").append(minorVersion);
		if (revision != 0) {
			version.append(".").append(revision);
		}
		return version.toString();
	}
	
	
}
