package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.command.CppcheckCommand;
import com.googlecode.cppcheclipse.core.command.ProcessExecutionException;

/**
 * This class should abstract from the eclipse concepts for easier testability.
 * Should not call static functions except 
 * @author kwindszus
 *
 */
public class Checker {
	
	final ProblemProfile profile;
	final CppcheckCommand command;
	final IProblemReporter problemReporter;
	final SuppressionProfile suppressionProfile;

	public Checker(IConsole console, IPreferenceStore projectPreferences, IPreferenceStore workspacePreferences, IProject project, Collection<String> includePaths, IProblemReporter problemReporter) throws XPathExpressionException,
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

		profile = CppcheclipsePlugin.getNewProblemProfile(console, problemPreferences);

		// check if we should use project or workspace preferences (for settings)
		IPreferenceStore settingsPreferences = projectPreferences;
		useWorkspacePreferences = projectPreferences
				.getBoolean(PreferenceConstants.SETTINGS_PAGE_ID
						+ PreferenceConstants.P_USE_PARENT_SUFFIX);

		if (useWorkspacePreferences) {
			settingsPreferences = workspacePreferences;
		}

		//includePaths = getIncludePaths();
		command = new CppcheckCommand(console, settingsPreferences, includePaths);
		
		this.problemReporter = problemReporter;
		
		suppressionProfile = new SuppressionProfile(projectPreferences, project);
	}

	public void processFile(String fileName, IFile file, IProgressMonitor monitor)
			throws CoreException, InterruptedException,
			XPathExpressionException, ParserConfigurationException, SAXException, IOException, ProcessExecutionException {
		if (suppressionProfile.isFileSuppressed(file))
			return;
		
		problemReporter.deleteMarkers(file);
		Collection<Problem> problems = command
					.run(fileName, file, monitor);

		// display each problem
		profile.reportProblems(problems, problemReporter, suppressionProfile);
	}

	
}
