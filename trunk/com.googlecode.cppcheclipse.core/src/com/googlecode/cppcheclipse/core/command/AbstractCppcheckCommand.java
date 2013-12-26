package com.googlecode.cppcheclipse.core.command;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.TeeOutputStream;
import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.utils.LineFilterOutputStream;

/**
 * Using Runtime.exec() for executing commands is very error-prone, therefore we
 * use the Apache Commons Exec library
 * 
 * @author kwindszus
 * 
 */
public abstract class AbstractCppcheckCommand {

	protected static final int SLEEP_TIME_MS = 100;
	protected static final String DEFAULT_CHARSET = "ASCII";

	private String binaryPath;

	private final IConsole console;
	private final String[] defaultArguments;
	private long startTime;
	private final Executor executor;
	protected final ExecuteWatchdog watchdog;
	private final ByteArrayOutputStream err, out;
	private CommandLine cmdLine;

	protected final LineFilterOutputStream processStdOut;
	protected final LineFilterOutputStream processStdErr;
	protected InputStream processStdIn;

	public AbstractCppcheckCommand(IConsole console, String[] defaultArguments,
			long timeout, String binaryPath) {

		this.binaryPath = binaryPath;
		this.console = console;
		this.defaultArguments = defaultArguments;
		
		executor = new DefaultExecutor();

		// all modes of operation returns 0 when no error occured,
		executor.setExitValue(0);

		watchdog = new ExecuteWatchdog(timeout);
		executor.setWatchdog(watchdog);

		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();

		processStdOut = new LineFilterOutputStream(new TeeOutputStream(out,
				console.getConsoleOutputStream(false)), DEFAULT_CHARSET);
		processStdErr = new LineFilterOutputStream(new TeeOutputStream(err,
				console.getConsoleOutputStream(true)), DEFAULT_CHARSET);
		
	}

	public AbstractCppcheckCommand(IConsole console, String[] defaultArguments, String binaryPath) {
		this(console, defaultArguments, ExecuteWatchdog.INFINITE_TIMEOUT, binaryPath);
	}

	/**
	 * The type is confusing but this gives the standard error of the process as
	 * buffered reader
	 * 
	 * @return standard error of process
	 * @throws UnsupportedEncodingException
	 */
	public BufferedReader getErrorReader() throws UnsupportedEncodingException {
		return new BufferedReader(new StringReader(getError()));
	}
	
	public void setProcessInputStream(InputStream inputStream) {
		processStdIn = inputStream;
	}

	/**
	 * The type is confusing but this gives the standard output of the process
	 * as buffered reader
	 * 
	 * @return standard output of process
	 * @throws UnsupportedEncodingException
	 */
	public BufferedReader getOutputReader() throws UnsupportedEncodingException {
		return new BufferedReader(new StringReader(getOutput()));
	}

	public InputStream getOutputStream() {
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * Returns the errors. After that the standard output is reset, to not
	 * return outputs twice.
	 * 
	 * @return standard output of process as string (with default charset)
	 * @throws UnsupportedEncodingException
	 */
	protected String getOutput() throws UnsupportedEncodingException {
		String output = out.toString(DEFAULT_CHARSET);
		out.reset();
		return output;
	}

	/**
	 * Returns the errors. After that the standard error output is reset, to not
	 * return errors twice.
	 * 
	 * @return standard error output of process as string (with default charset)
	 * @throws UnsupportedEncodingException
	 */
	protected String getError() throws UnsupportedEncodingException {
		String output = err.toString(DEFAULT_CHARSET);
		err.reset();
		return output;
	}

	static class CppcheckProcessResultHandler implements ExecuteResultHandler {

		private boolean isRunning = true;
		private int exitValue = 0;
		private ExecuteException exception = null;

		public synchronized void onProcessComplete(int exitValue) {
			isRunning = false;
			this.exitValue = exitValue;
		}

		public synchronized void onProcessFailed(ExecuteException exception) {
			isRunning = false;
			this.exception = exception;
		}

		public synchronized int getExitValue() throws ExecuteException {
			if (exception != null) {
				throw exception;
			}
			return exitValue;
		}

		public synchronized boolean isRunning() {
			return isRunning;
		}
	}

	protected CppcheckProcessResultHandler runInternal() throws IOException,
			ProcessExecutionException {
		return runInternal(null, null, null);
	}

	protected CppcheckProcessResultHandler runInternal(String[] arguments)
			throws IOException, ProcessExecutionException {
		return runInternal(arguments, null, null);
	}

	public void setWorkingDirectory(File dir) {
		executor.setWorkingDirectory(dir);
	}

	/**
	 * 
	 * 
	 * @param arguments
	 *            all given arguments must not start with a whitespace
	 *            (otherwise argument passing won't work)
	 * @param monitor
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ProcessExecutionException
	 */
	protected CppcheckProcessResultHandler runInternal(
			String[] singleArgumentsBefore, String multiArguments,
			String[] singleArgumentsAfter) throws IOException,
			ProcessExecutionException {
		if (binaryPath.length() == 0) {
			throw new EmptyPathException("No path to cppcheck binary given");
		}
		
		// TODO: check for existence of binary to improve error message in case binary could not be found!
		cmdLine = new CommandLine(binaryPath);

		// add default arguments
		if (defaultArguments != null) {
			cmdLine.addArguments(defaultArguments);
		}

		// don't add extra quoting to arguments (in the toString() method the
		// quoting is done nevertheless), arguments are merged together
		if (singleArgumentsBefore != null) {
			cmdLine.addArguments(singleArgumentsBefore, false);
		}
		if (multiArguments != null) {
			cmdLine.addArguments(multiArguments, false);
		}
		if (singleArgumentsAfter != null) {
			cmdLine.addArguments(singleArgumentsAfter, false);
		}
		
		executor.setStreamHandler(new PumpStreamHandler(processStdOut, processStdErr, processStdIn));

		CppcheckProcessResultHandler resultHandler = new CppcheckProcessResultHandler();

		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat();
		console.println("== Running cppcheck at " + format.format(date) + " ==");
		console.println("Command line: " + cmdLine.toString());

		startTime = System.currentTimeMillis();
		try {
			executor.execute(cmdLine, resultHandler);
		} catch (ExecuteException e) {
			// we need to rethrow the error to include the command line
			// since the error dialog does not display nested exceptions,
			// include original error string
			throw ProcessExecutionException.newException(cmdLine.toString(), e);
		}
		return resultHandler;
	}

	public void waitForExit(CppcheckProcessResultHandler resultHandler,
			IProgressMonitor monitor) throws InterruptedException,
			ProcessExecutionException, IOException {
		try {
			while (resultHandler.isRunning()) {
				Thread.sleep(SLEEP_TIME_MS);
				if (monitor.isCanceled()) {
					watchdog.destroyProcess();
					throw new InterruptedException("Process killed");
				}
			}
			// called to throw execution in case of invalid exit code
			resultHandler.getExitValue();
		} catch (ExecuteException e) {
			// we need to rethrow the error to include the command line
			// since the error dialog does not display nested exceptions,
			// include original error string
			throw ProcessExecutionException.newException(cmdLine.toString(), e);
		} finally {
			processStdErr.close();
			processStdOut.close();
		}
		long endTime = System.currentTimeMillis();
		console.println("Duration " + String.valueOf(endTime - startTime)
				+ " ms.");
	}

}
