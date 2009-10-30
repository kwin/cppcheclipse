package com.googlecode.cppcheclipse.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.cppcheclipse.core.IConsole;

public class UpdateCheckCommand {

	private static final String UPDATE_URL = "http://cppcheck.sourceforge.net/version.txt";

	public UpdateCheckCommand() {

	}

	private Version getNewVersion() throws IOException {
		URL url = new URL(UPDATE_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.setReadTimeout(10000);
		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("Wrong response code: "
					+ connection.getResponseMessage());
		}

		BufferedReader rd = new BufferedReader(new InputStreamReader(connection
				.getInputStream()));
		String line = rd.readLine();
		rd.close();
		if (line == null) {
			throw new IOException("Empty response");
		}
		return new Version(line);
	}
	
	private Version getCurrentVersion(IProgressMonitor monitor, IConsole console) throws IOException, InterruptedException, ProcessExecutionException {
		VersionCommand command = new VersionCommand(console);
		return command.run(monitor);
	}
	/**
	 * 
	 * @return version data of the new available version or null if no newer version available
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws ProcessExecutionException 
	 */
	public Version run(IProgressMonitor monitor, IConsole console) throws IOException, InterruptedException, ProcessExecutionException {
		Version newVersion = getNewVersion();
		Version currentVersion = getCurrentVersion(monitor, console);
		if (newVersion.isGreaterThan(currentVersion))
			return newVersion;
		return null;
	}
}
