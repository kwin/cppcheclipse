package com.googlecode.cppcheclipse.ui;

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
import org.eclipse.core.resources.IProject;

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

	public LanguageSettings(IProject project) {
		languageSettings = new LinkedList<ICLanguageSetting>();
		this.project = project;

		ICProjectDescription projectDescription = CoreModel.getDefault()
				.getProjectDescription(project);
		if (projectDescription == null) {
			throw new IllegalArgumentException("No valid CDT project given!");
		}

		ICConfigurationDescription activeConfiguration = projectDescription
				.getActiveConfiguration(); // or another config

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
	}

	public Collection<String> getUserIncludes() {
		return getIncludes(true);
	}

	public Collection<String> getSystemIncludes() {
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
	protected Collection<String> getIncludes(boolean onlyUserIncludes) {
		Collection<String> paths = new LinkedList<String>();
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
					String path = includePathSetting.getValue();
					// make workspace path absolute
					if ((includePathSetting.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == ICSettingEntry.VALUE_WORKSPACE_PATH) {
						path = workspacePath + path;
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
