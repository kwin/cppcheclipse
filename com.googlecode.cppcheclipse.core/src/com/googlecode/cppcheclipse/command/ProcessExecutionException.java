package com.googlecode.cppcheclipse.command;

public class ProcessExecutionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7085940287202256905L;

	public static ProcessExecutionException newException(String cmdLine, Throwable cause) {
		StringBuffer error = new StringBuffer();
		error.append("Error executing '").append(cmdLine).append("' due to error").append(cause.getLocalizedMessage());
		return new ProcessExecutionException(error.toString(), cause);
	}
	
	public ProcessExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessExecutionException(String message) {
		super(message);
	}
	
}
