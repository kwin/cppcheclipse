package com.googlecode.cppcheclipse.core.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.utils.IHttpClientService;

public class UpdateCheckCommand {

	private static final String UPDATE_URL = "http://cppcheck.sourceforge.net/version.txt";

	public UpdateCheckCommand() {

	}

	private Version getNewVersion() throws IOException, URISyntaxException {
		IHttpClientService client = CppcheclipsePlugin.getHttpClientService();
		InputStream is = client.executeGetRequest(new URL(UPDATE_URL));
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line = rd.readLine();
		rd.close();
		if (line == null) {
			throw new IOException("Empty response");
		}
		return new Version(line);
	}
	
	private Version getCurrentVersion(IProgressMonitor monitor, IConsole console, String binaryPath) throws IOException, InterruptedException, ProcessExecutionException {
		VersionCommand command = new VersionCommand(console, binaryPath);
		return command.run(monitor);
	}
	
	/**
	 * 
	 * @return version data of the new available version or null if no newer version available
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws ProcessExecutionException 
	 * @throws URISyntaxException 
	 */
	public Version run(IProgressMonitor monitor, IConsole console, String binaryPath) throws IOException, InterruptedException, ProcessExecutionException, URISyntaxException {
		Version newVersion = getNewVersion();
		Version currentVersion = getCurrentVersion(monitor, console, binaryPath);
		if (newVersion.isGreaterThan(currentVersion))
			return newVersion;
		return null;
	}
}
