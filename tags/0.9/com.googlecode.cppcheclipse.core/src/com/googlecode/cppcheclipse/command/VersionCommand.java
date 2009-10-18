package com.googlecode.cppcheclipse.command;

import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;

public class VersionCommand extends AbstractCppCheckCommand {
	private static final String ARGUMENT = "--version";
	
	public Version run() throws IOException, InterruptedException {
		String arguments = ARGUMENT;
		CppcheckProcess process = run(arguments, new NullProgressMonitor());
		String version = process.getStdOut();
		process.close();
		return new Version(version);
	}
}
