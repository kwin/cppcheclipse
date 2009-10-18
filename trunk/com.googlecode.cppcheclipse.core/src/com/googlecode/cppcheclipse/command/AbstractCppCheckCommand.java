package com.googlecode.cppcheclipse.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.command.Console.ConsoleInputStream;
import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;
import com.googlecode.cppcheclipse.core.Problem;

public abstract class AbstractCppCheckCommand {

	private static final String DEFAULT_ARGUMENTS = " ";

	private static final int SLEEP_TIME_MS = 100;

	private String binaryPath;

	private final Console console;

	public AbstractCppCheckCommand() {
		binaryPath = CppcheclipsePlugin.getConfigurationPreferenceStore()
				.getString(PreferenceConstants.P_BINARY_PATH);
		console = new Console();
	}

	public class CppcheckProcess {
		private static final int DEFAULT_BUFFER_SIZE = 128;

		private static final String DEFAULT_CHARSET = "ASCII";

		private final ConsoleInputStream output, error;
		private final int exitValue;
		
		public CppcheckProcess(Process process) {
			error = console.createInputStream(process.getErrorStream());
			output = console.createInputStream(process.getInputStream());
			exitValue = process.exitValue();
		}

		public int getExitValue() {
			return exitValue;
		}

		public ConsoleInputStream getErrorStream() {
			return error;
		}
		
		/**
		 * The name is confusing but this gives the standard output of the process
		 * @return
		 */
		public ConsoleInputStream getOutputStream() {
			return output;
		}
		
		protected String getStdOut() throws IOException {
			InputStream is = getOutputStream();
			StringWriter writer = new StringWriter();
			InputStreamReader reader = new InputStreamReader(is, DEFAULT_CHARSET);
			copy(reader, writer);
			return writer.getBuffer().toString();
		}

		protected String getStdErr() throws IOException {
			InputStream is = getErrorStream();
			StringWriter writer = new StringWriter();
			InputStreamReader reader = new InputStreamReader(is, DEFAULT_CHARSET);
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
			error.close();
			output.close();
		}
		
	}

	protected CppcheckProcess run(String arguments, IProgressMonitor monitor)
			throws IOException, InterruptedException {
		String cmdLine = binaryPath + DEFAULT_ARGUMENTS + arguments;
		console.print("Executing " + cmdLine);
		Process p = Runtime.getRuntime().exec(cmdLine);
		// wait till process has finished
		WaitForProcessEndThread thread = new WaitForProcessEndThread(p);
		thread.start();

		while (thread.isAlive()) {
			Thread.sleep(SLEEP_TIME_MS);
			if (monitor.isCanceled()) {
				thread.join(SLEEP_TIME_MS);
				throw new InterruptedException("Process killed");
			}
		}
		return new CppcheckProcess(p);
	}

	private static class WaitForProcessEndThread extends Thread {
		private final Process process;

		private WaitForProcessEndThread(Process process) {
			this.process = process;
		}

		public void run() {
			try {
				process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
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
	public static Map<String, Problem> parseXMLStream(InputStream stream,
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
		Map<String, Problem> problems = new HashMap<String, Problem>();
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
			if (!line.isEmpty()) {
				lineNumber = Integer.parseInt(line);
			}

			// add file optionally
			String filename = xpath.evaluate("@file", error);

			problems.put(id, new Problem(id, msg, severity, file, filename,
					lineNumber));
		}
		return problems;
	}
}
