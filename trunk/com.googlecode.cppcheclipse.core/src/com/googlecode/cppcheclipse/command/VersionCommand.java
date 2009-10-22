package com.googlecode.cppcheclipse.command;

import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;

public class VersionCommand extends AbstractCppCheckCommand {
	private static final String[] ARGUMENTS = {"--version"};
	private static final int TIMEOUT_MS = 500;
	
	public VersionCommand() {
		super(TIMEOUT_MS);
	}
	
	public Version run() throws IOException, InterruptedException {
		CppcheckProcess process = run(ARGUMENTS, new NullProgressMonitor());
		String version = process.getOutput();
		process.close();
		return new Version(version);
	}
}
