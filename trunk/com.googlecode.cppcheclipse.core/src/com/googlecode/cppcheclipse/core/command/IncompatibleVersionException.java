package com.googlecode.cppcheclipse.core.command;

public class IncompatibleVersionException extends ProcessExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6032181437996210576L;

	public IncompatibleVersionException(Version version) {
		super("You must have at least version " + Version.MIN_VERSION.toString() + " of cppcheck. But you only have version "+ version.toString());
	}

}
