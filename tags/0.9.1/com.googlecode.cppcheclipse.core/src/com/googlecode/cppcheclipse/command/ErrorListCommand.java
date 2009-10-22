package com.googlecode.cppcheclipse.command;

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.Problem;

public class ErrorListCommand extends AbstractCppCheckCommand {

	private static final String[] ARGUMENTS = {"--errorlist"};
	private static final int TIMEOUT_MS = 1000;

	public ErrorListCommand() {
		super(TIMEOUT_MS);
	}
	
	public Collection<Problem> run() throws IOException, InterruptedException,
			XPathExpressionException, ParserConfigurationException,
			SAXException {
		CppcheckProcess process = run(ARGUMENTS, new NullProgressMonitor());
		Collection<Problem> problems = parseXMLStream(process.getOutputStream(), null);
		process.close();
		return problems;
	}
}
