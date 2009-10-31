package com.googlecode.cppcheclipse.core.command;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.cppcheclipse.core.IConsole;

public class VersionCommand extends AbstractCppcheckCommand {
	private static final String[] ARGUMENTS = {"--version"};
	private static final int TIMEOUT_MS = 500;
	
	public VersionCommand(IConsole console) {
		super(TIMEOUT_MS, console);
	}
	
	public Version run(IProgressMonitor monitor) throws IOException, InterruptedException, ProcessExecutionException {
		CppcheckProcess process = run(ARGUMENTS, monitor);
		String version = process.getOutput();
		process.close();
		return new Version(version);
	}
}
