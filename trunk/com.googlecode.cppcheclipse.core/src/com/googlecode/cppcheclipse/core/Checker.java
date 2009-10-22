package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.command.CppCheckCommand;

/**
 * This class should abstract from the eclipse concepts for easier testability.
 * Should not call static functions except 
 * @author kwindszus
 *
 */
public class Checker {
	
	final ProblemProfile profile;
	//final Collection<String> includePaths;
	final CppCheckCommand command;

	public Checker(IPreferenceStore projectPreferences, IPreferenceStore workspacePreferences, Collection<String> includePaths) throws XPathExpressionException,
			IOException, InterruptedException, ParserConfigurationException,
			SAXException, CloneNotSupportedException {
		
		// check if we should use project or workspace preferences (for problems)
		IPreferenceStore problemPreferences = projectPreferences;
		boolean useWorkspacePreferences = projectPreferences
				.getBoolean(PreferenceConstants.PROBLEMS_PAGE_ID
						+ PreferenceConstants.P_USE_PARENT_SUFFIX);

		if (useWorkspacePreferences) {
			problemPreferences = workspacePreferences;
		}

		profile = CppcheclipsePlugin.getNewProblemProfile(problemPreferences);

		// check if we should use project or workspace preferences (for settings)
		IPreferenceStore settingsPreferences = projectPreferences;
		useWorkspacePreferences = projectPreferences
				.getBoolean(PreferenceConstants.SETTINGS_PAGE_ID
						+ PreferenceConstants.P_USE_PARENT_SUFFIX);

		if (useWorkspacePreferences) {
			settingsPreferences = workspacePreferences;
		}

		//includePaths = getIncludePaths();
		command = new CppCheckCommand(settingsPreferences, includePaths);
	}

	public void processFile(String fileName, IFile file, IProgressMonitor monitor)
			throws CoreException, InterruptedException,
			XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		ProblemReporter.deleteMarkers(file);
		Collection<Problem> problems = command
					.run(fileName, file, monitor);

		// display each problem
		profile.reportProblems(problems);
	}

	
}
