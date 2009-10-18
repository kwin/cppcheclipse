package com.googlecode.cppcheclipse.command;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.PreferenceConstants;
import com.googlecode.cppcheclipse.core.Problem;

public class CppCheckCommand extends AbstractCppCheckCommand {
	private final static String ARGUMENTS = "--xml";

	private final StringBuffer arguments;
	
	public CppCheckCommand(IPreferenceStore store, Collection<String> includePaths) {
		arguments = new StringBuffer(ARGUMENTS);
		
		if (store.getBoolean(PreferenceConstants.P_CHECK_ALL)) {
			arguments.append(" --all");
		}
		
		if (store.getBoolean(PreferenceConstants.P_CHECK_STYLE)) {
			arguments.append(" --style");
		}
		
		if (store.getBoolean(PreferenceConstants.P_CHECK_UNUSED_FUNCTIONS)) {
			arguments.append(" --unused-functions");
		}
		
		arguments.append(" -j ").append(store.getInt(PreferenceConstants.P_NUMBER_OF_THREADS));
		
		if (store.getBoolean(PreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES)) {
			for (String path: includePaths) {
				arguments.append(" -I ").append(path);
			}
		}
	}
	
	public Map<String, Problem> run(String filename, IFile file, IProgressMonitor monitor)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException, InterruptedException {
		
		CppcheckProcess process = run(arguments.toString() + " " + filename, monitor);
		// check exit code
		if (process.getExitValue() != 0) {
			throw new IOException("Invalid exit code. Stderr: " + process.getStdErr() + "\nStdout: " + process.getStdOut());
		}
		Map<String, Problem> problems = parseXMLStream(process.getErrorStream(), file);
		process.close();
		return problems;
	}
}
