package com.googlecode.cppcheclipse.core.command;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Version {

	private static final String VERSION_PREFIX = "cppcheck";
	private static final String DELIMITER = ".";
	private final int majorVersion;
	private final int minorVersion;
	private final int revision;
	
	public static final Version MIN_VERSION = new Version (1, 40, 0);
	
	private Version(int majorVersion, int minorVersion, int revision) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.revision = revision;
	}	
	
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
	
	public boolean isGreaterOrEqual(Version version) {
		boolean result = isGreaterThan(version);
		if (!result) {
			// check for equality
			if (equals(version)) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + majorVersion;
		result = prime * result + minorVersion;
		result = prime * result + revision;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (majorVersion != other.majorVersion)
			return false;
		if (minorVersion != other.minorVersion)
			return false;
		if (revision != other.revision)
			return false;
		return true;
	}
	
}
