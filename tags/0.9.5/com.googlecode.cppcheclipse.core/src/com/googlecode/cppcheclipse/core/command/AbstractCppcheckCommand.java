package com.googlecode.cppcheclipse.core.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.core.Problem;

/**
 * Using Runtime.exec() for executing commands is very error-prone, therefore we
 * use the Apache Commons Exec library
 * 
 * @author kwindszus
 * 
 */
public abstract class AbstractCppcheckCommand {

	private static final int SLEEP_TIME_MS = 100;

	private static final int WATCHDOG_TIMEOUT_MS = 90000;

	private String binaryPath;

	private final IConsole console;
	private final int timeout;

	public AbstractCppcheckCommand(int timeout, IConsole console) {
		binaryPath = CppcheclipsePlugin.getConfigurationPreferenceStore()
				.getString(IPreferenceConstants.P_BINARY_PATH);
		this.console = console;
		this.timeout = timeout;
	}

	public AbstractCppcheckCommand(IConsole console) {
		this(WATCHDOG_TIMEOUT_MS, console);
	}

	public class CppcheckProcess {
		private static final int DEFAULT_BUFFER_SIZE = 128;
		private static final String DEFAULT_CHARSET = "ASCII";

		private final InputStream output, error;
		private final int exitValue;

		public CppcheckProcess(int exitValue, byte[] output, byte[] error) {
			this.exitValue = exitValue;
			this.output = new ByteArrayInputStream(output);
			this.error = new ByteArrayInputStream(error);
		}

		public int getExitValue() {
			return exitValue;
		}

		public InputStream getErrorStream() {
			return error;
		}

		/**
		 * The name is confusing but this gives the standard output of the
		 * process
		 * 
		 * @return
		 */
		public InputStream getOutputStream() {
			return output;
		}

		protected String getOutput() throws IOException {
			InputStream is = getOutputStream();
			StringWriter writer = new StringWriter();
			InputStreamReader reader = new InputStreamReader(is,
					DEFAULT_CHARSET);
			copy(reader, writer);
			return writer.getBuffer().toString();
		}

		protected String getError() throws IOException {
			InputStream is = getErrorStream();
			StringWriter writer = new StringWriter();
			InputStreamReader reader = new InputStreamReader(is,
					DEFAULT_CHARSET);
			copy(reader, writer);
			return writer.getBuffer().toString();
		}

		/**
		 * Copy chars from a Reader to a Writer.
		 * 
		 * @param input
		 *            the Reader to read from
		 * @param output
		 *            the Writer to write to
		 * @return the number of characters copied
		 * @throws IOException
		 *             In case of an I/O problem
		 */
		private int copy(Reader input, Writer output) throws IOException {
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			int count = 0;
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
			return count;
		}

		public void close() throws IOException {
			// streams may be closed before (by using the XMLParser)
			try {
				error.close();
			} catch (IOException e) {

			}
			try {
				output.close();
			} catch (IOException e) {

			}
		}
	}

	private class CppcheckProcessResultHandler implements ExecuteResultHandler {

		private boolean isRunning = true;
		private int exitValue = 0;
		private ExecuteException exception = null;

		synchronized public void onProcessComplete(int exitValue) {
			isRunning = false;
			this.exitValue = exitValue;
		}

		synchronized public void onProcessFailed(ExecuteException exception) {
			isRunning = false;
			this.exception = exception;
		}

		public int getExitValue() throws ExecuteException {
			if (exception != null) {
				throw exception;
			}
			return exitValue;
		}

		synchronized public boolean isRunning() {
			return isRunning;
		}
	}

	/**
	 * 
	 * 
	 * @param arguments all given arguments must not start with a whitespace (otherwise argument passing won't work)
	 * @param monitor
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ProcessExecutionException
	 */
	protected CppcheckProcess run(String[] arguments, IProgressMonitor monitor)
			throws IOException, InterruptedException, ProcessExecutionException {
		if (binaryPath.length() == 0) {
			throw new EmptyPathException(
					"No path to cppcheck binary given");
		}
		// argument contains only the executable (may contain spaces)
		CommandLine cmdLine = new CommandLine(binaryPath);
		cmdLine.addArguments(arguments, false); // don't add extra quoting (in the toString() method the quoting is done nevertheless)

		DefaultExecutor executor = new DefaultExecutor();
		// @see bug http://sourceforge.net/apps/trac/cppcheck/ticket/824 (so far
		// accept also wrong exit values)
		
		
		// TODO: set to zero as soon as ticket 824 is fixed, still open what to do with older versions
		executor.setExitValues(null);
	
		ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream err, out;
		err = console.createByteArrayOutputStream(true);
		out = console.createByteArrayOutputStream(false);
		executor.setStreamHandler(new PumpStreamHandler(out, err));

		CppcheckProcessResultHandler resultHandler = new CppcheckProcessResultHandler();

		
		console.println("Executing '" + cmdLine.toString() + "'");
		long startTime = System.currentTimeMillis();
		int exitValue = 0;
		try {
		executor.execute(cmdLine, resultHandler);

		while (resultHandler.isRunning()) {
			Thread.sleep(SLEEP_TIME_MS);
			if (monitor.isCanceled()) {
				watchdog.destroyProcess();
				throw new InterruptedException("Process killed");
			}
		}

		exitValue = resultHandler.getExitValue();
			
		} catch (ExecuteException e) {
			// we need to rethrow the error to include the command line
			// since the error dialog does not display nested exceptions, include original error string
			throw ProcessExecutionException.newException(cmdLine.toString(), e);
		} finally {
			err.close();
			out.close();
		}
		long endTime = System.currentTimeMillis();
		console.println("Duration " + String.valueOf(endTime - startTime)
				+ " ms.");
		return new CppcheckProcess(exitValue, out.toByteArray(), err
				.toByteArray());
	}

	/**
	 * Possibility to overwrite binary path
	 * 
	 * @param binaryPath
	 */
	public void setBinaryPath(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	/**
	 * Can handle both formats (--errorlist as well as checking itself) Format:
	 * <results> <error id="autoVariables" severity="error" msg=
	 * "Wrong assignement of an auto-variable to an effective parameter of a function"
	 * /> </result>
	 * 
	 * or
	 * 
	 * <results> <error file="a.cpp" line="4" id="arrayIndexOutOfBounds"
	 * severity="all" msg="Array index out of bounds"/> </results>
	 * 
	 * @param stream
	 * @return
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Collection<Problem> parseXMLStream(InputStream stream,
			IFile file) throws ParserConfigurationException,
			XPathExpressionException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		documentBuilderFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document doc = documentBuilder.parse(stream);

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		NodeList errors = (NodeList) xpath.evaluate("//error", doc,
				XPathConstants.NODESET);
		Collection<Problem> problems = new LinkedList<Problem>();
		// can't use iterator here
		for (int i = 0; i < errors.getLength(); i++) {
			Node error = errors.item(i);
			String id = (String) xpath.evaluate("@id", error,
					XPathConstants.STRING);
			String msg = (String) xpath.evaluate("@msg", error,
					XPathConstants.STRING);
			String severity = (String) xpath.evaluate("@severity", error,
					XPathConstants.STRING);

			// add line optionally
			String line = xpath.evaluate("@line", error);
			int lineNumber = 0;
			if (line.length() > 0) {
				lineNumber = Integer.parseInt(line);
			}

			// add file optionally
			String filename = xpath.evaluate("@file", error);

			problems.add(new Problem(id, msg, severity, file, filename,
					lineNumber));
		}
		return problems;
	}
}
