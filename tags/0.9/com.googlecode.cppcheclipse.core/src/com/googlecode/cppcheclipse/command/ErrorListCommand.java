package com.googlecode.cppcheclipse.command;

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.Problem;

public class ErrorListCommand extends AbstractCppCheckCommand {

	private static final String ARGUMENT = "--errorlist";

	public Collection<Problem> run() throws IOException, InterruptedException,
			XPathExpressionException, ParserConfigurationException,
			SAXException {
		CppcheckProcess process = run(ARGUMENT, new NullProgressMonitor());
		Collection<Problem> problems = parseXMLStream(process.getOutputStream(), null);
		process.close();
		return problems;
	}

	/**
	 * Format of XML is described here
	 * 
	 * 
	 * <?xml version="1.0"?> <results> <error id="autoVariables"
	 * severity="error" msg=
	 * "Wrong assignement of an auto-variable to an effective parameter of a function"
	 * /> </result>
	 * 
	 * @param errorStream
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 * 
	 *             protected void parseXMLErrorList(InputStream errorStream,
	 *             ProblemProfile profile) throws ParserConfigurationException,
	 *             SAXException, IOException, XPathExpressionException {
	 * 
	 *             XPathFactory xpathFactory = XPathFactory.newInstance(); XPath
	 *             xpath = xpathFactory.newXPath();
	 * 
	 *             NodeList errors = parseXMLStream(errorStream); for (int i =
	 *             0; i < errors.getLength(); i++) { Node error =
	 *             errors.item(i); String id = (String)xpath.evaluate("@id",
	 *             error, XPathConstants.STRING); String msg =
	 *             (String)xpath.evaluate("@msg", error, XPathConstants.STRING);
	 *             String severity = (String)xpath.evaluate("@severity", error,
	 *             XPathConstants.STRING);
	 * 
	 *             profile.addProblem(new Problem(id, msg, severity)); } }
	 */

	void addProblemType(String id, String name, String severity) {
		/*
		 * CodanProblem problem = new CodanProblem(id, name); CodanSeverity sev;
		 * if (severity == "style") { sev = CodanSeverity.Warning;
		 * 
		 * } else if (severity == "error") { sev = CodanSeverity.Error;
		 * 
		 * } else { // possible error sev = CodanSeverity.Info; }
		 * problem.setSeverity(sev);
		 * 
		 * CheckersRegisry registry = CheckersRegisry.getInstance();
		 * registry.addProblem(problem, STANDARD_CATEGORY);
		 */
	}
}
