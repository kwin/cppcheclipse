package com.googlecode.cppcheclipse.core.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.Problem;

public class ErrorListCommand extends AbstractCppcheckCommand {

	private static final String[] DEFAULT_ARGUMENTS = {"--errorlist"};
	private static final int TIMEOUT_MS = 1000;

	public ErrorListCommand(IConsole console) {
		super(console, DEFAULT_ARGUMENTS, TIMEOUT_MS);
	}
	
	public Collection<Problem> run() throws IOException, InterruptedException,
			XPathExpressionException, ParserConfigurationException,
			SAXException, ProcessExecutionException {
		CppcheckProcessResultHandler resultHandler = runInternal();
		waitForExit(resultHandler,  new NullProgressMonitor());
		Collection<Problem> problems = parseXMLStream(getOutputStream(), null);
		return problems;
	}
	
	/**
	 * Can handle both formats (--errorlist as well as file checking). Format:
	 * &lt;results&gt; &lt;error id="autoVariables" severity="error" msg=
	 * "Wrong assignement of an auto-variable to an effective parameter of a function"
	 * /&gt; &lt;/result&gt;
	 * 
	 * or
	 * 
	 * &lt;results&gt; &lt;error file="a.cpp" line="4" id="arrayIndexOutOfBounds"
	 * severity="all" msg="Array index out of bounds"/&gt; &lt;/results&gt;
	 * 
	 * @param stream
	 * @return Collection of Problems
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

			problems.add(new Problem(id, msg, severity));
		}
		return problems;
	}
}
