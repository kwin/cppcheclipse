package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

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
 * 
 * @author kwindszus
 * 
 */
public class Checker {

	private final ProblemProfile profile;
	private final CppcheckCommand command;
	private final IProblemReporter problemReporter;
	private final SuppressionProfile suppressionProfile;
	private final IProject project;
	private List<IFile> files;

	public Checker(IConsole console, IPreferenceStore projectPreferences,
			IPreferenceStore workspacePreferences, IProject project,
			IToolchainSettings toolchainSettings,
			IProblemReporter problemReporter) throws XPathExpressionException,
			IOException, InterruptedException, ParserConfigurationException,
			SAXException, CloneNotSupportedException, ProcessExecutionException {

		// check if we should use project or workspace preferences (for
		// problems)
		IPreferenceStore problemPreferences = projectPreferences;
		boolean useWorkspacePreferences = projectPreferences
				.getBoolean(IPreferenceConstants.PROBLEMS_PAGE_ID
						+ IPreferenceConstants.P_USE_PARENT_SUFFIX);

		if (useWorkspacePreferences) {
			problemPreferences = workspacePreferences;
		}

		profile = CppcheclipsePlugin.getNewProblemProfile(console,
				problemPreferences);

		// check if we should use project or workspace preferences (for
		// settings)
		IPreferenceStore settingsPreferences = projectPreferences;
		useWorkspacePreferences = projectPreferences
				.getBoolean(IPreferenceConstants.SETTINGS_PAGE_ID
						+ IPreferenceConstants.P_USE_PARENT_SUFFIX);

		if (useWorkspacePreferences) {
			settingsPreferences = workspacePreferences;
		}

		Symbols symbols;
		// restrict configurations to the given macros
		if (projectPreferences.getBoolean(IPreferenceConstants.P_RESTRICT_CONFIGURATION_CHECK)) {
			symbols = new Symbols(projectPreferences, toolchainSettings);
		} else {
			symbols = new Symbols();
		}
		
		String binaryPath = CppcheclipsePlugin.getConfigurationPreferenceStore()
		.getString(IPreferenceConstants.P_BINARY_PATH);
		
		command = new CppcheckCommand(console, binaryPath, settingsPreferences,
				projectPreferences, toolchainSettings.getUserIncludes(), toolchainSettings.getSystemIncludes(), symbols);
		this.problemReporter = problemReporter;
		suppressionProfile = new SuppressionProfile(projectPreferences, project);

		files = new LinkedList<IFile>();
		this.project = project;
	}

	public void addFile(IFile file) throws CoreException {
		if (suppressionProfile.isFileSuppressed(file.getLocation().toFile()))
			return;
		if (file.getProject() != project) {
			throw new IllegalArgumentException(
					"Only files within the project are valid");
		}
		problemReporter.deleteMarkers(file, true);
		files.add(file);
	}

	public void run(IProgressMonitor monitor, IProgressReporter progressReporter) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException,
			InterruptedException, ProcessExecutionException, CoreException, URISyntaxException {
		if (files.isEmpty()) {
			return;
		}
		
		// TODO: always reset external resource markers (since we have no way of identifying why this external resource was checked so far)
		problemReporter.deleteMarkers(project, false);
		command.run(this, progressReporter, project, files, monitor);
	}

	/**
	 * Callback, called from CppcheckCommand
	 * 
	 * @param problems
	 * @throws CoreException
	 */
	public void reportProblems(List<Problem> problems) throws CoreException {
		// display each problem
		profile.reportProblems(problems, problemReporter, suppressionProfile);
	}
}
