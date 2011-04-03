package com.googlecode.cppcheclipse.core.command;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.cppcheclipse.core.IConsole;

public class VersionCommand extends AbstractCppcheckCommand {
	private static final String[] DEFAULT_ARGUMENTS = {"--version"};
	private static final int TIMEOUT_MS = 5000;
	
	public VersionCommand(IConsole console, String binaryPath) {
		super(console, DEFAULT_ARGUMENTS, TIMEOUT_MS, binaryPath);
	}
	
	public Version run(IProgressMonitor monitor) throws IOException, InterruptedException, ProcessExecutionException {
		CppcheckProcessResultHandler resultHandler = runInternal();
		waitForExit(resultHandler,  monitor);
		return new Version(getOutput());
	}
}
