package com.googlecode.cppcheclipse.ui;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Getting some settings done in the toolchain of CDT
 * 
 * @see <a
 *      href="http://cdt-devel-faq.wikidot.com/#toc21">http://cdt-devel-faq.wikidot.com/#toc21</a>
 * @author kwindszus
 * 
 */
public class LanguageSettings {
	private final List<ICLanguageSetting> languageSettings;
	private final IProject project;
	private final IWorkspaceRoot root;

	public LanguageSettings(IProject project) throws IllegalStateException {
		languageSettings = new LinkedList<ICLanguageSetting>();
		this.project = project;

		ICProjectDescription projectDescription = CoreModel.getDefault()
				.getProjectDescription(project);
		if (projectDescription == null) {
			throw new IllegalStateException("No valid CDT project given!");
		}

		ICConfigurationDescription activeConfiguration = projectDescription
				.getActiveConfiguration(); // or another config
		if (activeConfiguration == null) {
			throw new IllegalStateException("No valid active configuration found!");
		}
		ICFolderDescription folderDescription = activeConfiguration
				.getRootFolderDescription(); // or use
		// getResourceDescription(IResource),
		// or pick one from
		// getFolderDescriptions()
		ICLanguageSetting[] allLanguageSettings = folderDescription
				.getLanguageSettings();

		// fetch the include settings from the first tool which supports c
		for (ICLanguageSetting languageSetting : allLanguageSettings) {
			String extensions[] = languageSetting.getSourceExtensions();
			for (String extension : extensions) {
				if ("cpp".equalsIgnoreCase(extension)) { //$NON-NLS-1$
					languageSettings.add(languageSetting);
				}
			}
		}
		
		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	public Collection<File> getUserIncludes() {
		return getIncludes(true);
	}

	public Collection<File> getSystemIncludes() {
		return getIncludes(false);
	}

	/**
	 * Gets all the include folders from the current configuration.
	 * 
	 * @param onlyUserIncludes
	 *            if true, only user-defined includes are returned, otherwise
	 *            only system-defined.
	 * @return all include folders in a list
	 */
	protected Collection<File> getIncludes(boolean onlyUserIncludes) {
		Collection<File> paths = new LinkedList<File>();
		String workspacePath = project.getWorkspace().getRoot().getLocation()
				.toOSString();

		for (ICLanguageSetting languageSetting : languageSettings) {
			ICLanguageSettingEntry[] includePathSettings = languageSetting
					.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
			for (ICLanguageSettingEntry includePathSetting : includePathSettings) {
				// only regard user-specified include paths or only
				// system include paths
				if ((!includePathSetting.isBuiltIn() && onlyUserIncludes)
						|| (includePathSetting.isBuiltIn() && !onlyUserIncludes)) {
					File path = new File(includePathSetting.getValue());
					// make workspace path absolute
					if ((includePathSetting.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == ICSettingEntry.VALUE_WORKSPACE_PATH) {
						path = new File(workspacePath, path.toString());
						
					}
					// resolve workspace path, since it may contain linked resources
					IFile file = root.getFileForLocation(new Path(path.toString()));
					if (file != null) {
						path = file.getLocation().toFile();
					}
					paths.add(path);
				}
			}
		}
		return paths;
	}

	public Collection<String> getMacros() {
		Collection<String> macros = new LinkedList<String>();

		for (ICLanguageSetting languageSetting : languageSettings) {
			ICLanguageSettingEntry[] macroSettings = languageSetting
					.getSettingEntries(ICSettingEntry.MACRO);
			for (ICLanguageSettingEntry macroSetting : macroSettings) {
				macros.add(macroSetting.getName() + ";Value:"
						+ macroSetting.getValue());
			}
		}
		return macros;
	}
}
