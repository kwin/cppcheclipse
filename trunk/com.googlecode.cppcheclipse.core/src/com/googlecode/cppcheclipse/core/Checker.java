package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.command.CppCheckCommand;

public class Checker {

	private static final String[] VALID_EXTENSIONS = { ".cpp", ".cxx", ".c++",
			".cc", ".c" };

	private final ProblemProfile profile;
	private final IProject project;
	private final Collection<String> includePaths;
	private final CppCheckCommand command;

	public Checker(IProject project) throws XPathExpressionException,
			IOException, InterruptedException, ParserConfigurationException,
			SAXException, CloneNotSupportedException {
		this.project = project;
		// check if we should use project or workspace preferences (for problems)
		IPreferenceStore problemPreferences = CppcheclipsePlugin
				.getProjectPreferenceStore(project, false);
		boolean useWorkspacePreferences = problemPreferences
				.getBoolean(PreferenceConstants.PROBLEMS_PAGE_ID
						+ PreferenceConstants.P_USE_PARENT_SUFFIX);

		if (useWorkspacePreferences) {
			problemPreferences = CppcheclipsePlugin
					.getWorkspacePreferenceStore();
		}

		profile = CppcheclipsePlugin.getNewProblemProfile(problemPreferences);

		// check if we should use project or workspace preferences (for settings)
		IPreferenceStore settingsPreferences = CppcheclipsePlugin
				.getProjectPreferenceStore(project, false);
		useWorkspacePreferences = settingsPreferences
				.getBoolean(PreferenceConstants.SETTINGS_PAGE_ID
						+ PreferenceConstants.P_USE_PARENT_SUFFIX);

		if (useWorkspacePreferences) {
			settingsPreferences = CppcheclipsePlugin
					.getWorkspacePreferenceStore();
		}

		includePaths = getIncludePaths();
		command = new CppCheckCommand(settingsPreferences, includePaths);
	}

	public boolean isValidProject(IResource resource) {
		return (project != resource.getProject());
	}

	public boolean processResource(IResource resource, IProgressMonitor monitor)
			throws XPathExpressionException, CoreException,
			InterruptedException, ParserConfigurationException, SAXException,
			IOException {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			processFile(file, monitor);
			return false;
		}
		return false;
	}

	private void processFile(IFile file, IProgressMonitor monitor)
			throws CoreException, InterruptedException,
			XPathExpressionException, ParserConfigurationException,
			SAXException, IOException {
		// create translation unit and access index
		String fileName = file.getLocation().makeAbsolute().toOSString();
		if (shouldCheck(fileName)) {
			ProblemReporter.deleteMarkers(file);
			Collection<Problem> problems = command
					.run(fileName, file, monitor);

			// display each problem
			profile.reportProblems(problems);
		}
	}

	private boolean shouldCheck(String filename) {
		// check for valid extension
		for (int n = 0; n < VALID_EXTENSIONS.length; n++) {
			if (filename.endsWith(VALID_EXTENSIONS[n]))
				return true;
		}
		return false;
	}

	/**
	 * @see http://cdt-devel-faq.wikidot.com/#toc21
	 * @return
	 */
	private Collection<String> getIncludePaths() {
		Collection<String> paths = new LinkedList<String>();
		String workspacePath = project.getWorkspace().getRoot()
				.getRawLocation().makeAbsolute().toOSString();

		ICProjectDescription projectDescription = CoreModel.getDefault()
				.getProjectDescription(project);
		if (projectDescription == null) {
			return paths;
		}
		ICConfigurationDescription activeConfiguration = projectDescription
				.getActiveConfiguration(); // or another config
		if (activeConfiguration == null) {
			return paths;
		}
		ICFolderDescription folderDescription = activeConfiguration
				.getRootFolderDescription(); // or use
		// getResourceDescription(IResource),
		// or pick one from
		// getFolderDescriptions()
		ICLanguageSetting[] languageSettings = folderDescription
				.getLanguageSettings();

		// fetch the include settings from the first tool which supports c
		for (ICLanguageSetting languageSetting : languageSettings) {
			String extensions[] = languageSetting.getSourceExtensions();
			for (String extension : extensions) {
				if ("cpp".equalsIgnoreCase(extension)) {
					ICLanguageSettingEntry[] includePathSettings = languageSetting
							.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
					for (ICLanguageSettingEntry includePathSetting : includePathSettings) {
						String path = includePathSetting.getValue();
						if ((includePathSetting.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == ICSettingEntry.VALUE_WORKSPACE_PATH) {
							path = workspacePath + path;
						}
						paths.add(path);
					}
				}
			}
			if (paths.size() > 0) {
				return paths;
			}
		}
		return paths;
	}
}
