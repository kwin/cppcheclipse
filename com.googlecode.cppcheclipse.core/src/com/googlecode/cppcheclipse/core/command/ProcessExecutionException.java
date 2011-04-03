package com.googlecode.cppcheclipse.core.command;

public class ProcessExecutionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7085940287202256905L;

	public static ProcessExecutionException newException(String cmdLine, Throwable cause) {
		StringBuffer errorMsg = new StringBuffer();
		errorMsg.append("Error executing '").append(cmdLine).append("' due to error: ").append(cause.getLocalizedMessage());
		errorMsg.append(" Maybe more information is available in the console view.");
		return new ProcessExecutionException(errorMsg.toString(), cause);
	}
	
	public ProcessExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessExecutionException(String message) {
		super(message);
	}
	
}
