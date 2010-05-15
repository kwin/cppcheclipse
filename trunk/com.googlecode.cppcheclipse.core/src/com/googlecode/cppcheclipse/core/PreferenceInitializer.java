package com.googlecode.cppcheclipse.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		// only initialize workspace preferences (project preferences will get that too, since they inherit the values)
		IPreferenceStore store = CppcheclipsePlugin.getWorkspacePreferenceStore();
		initializeBinaryPathPreferencesDefault(CppcheclipsePlugin.getConfigurationPreferenceStore());
		initializeSettingsDefault(store);
		initializeProblemsDefault(store);
	}
	
	private static void initializeSettingsDefault(IPreferenceStore store) {
		store.setDefault(IPreferenceConstants.P_CHECK_ALL, false);
		store.setDefault(IPreferenceConstants.P_CHECK_STYLE, false);
		store.setDefault(IPreferenceConstants.P_CHECK_VERBOSE, false);
		store.setDefault(IPreferenceConstants.P_CHECK_FORCE, false);
		store.setDefault(IPreferenceConstants.P_CHECK_DEBUG, false);
		store.setDefault(IPreferenceConstants.P_CHECK_EXCEPT_NEW, false);
		store.setDefault(IPreferenceConstants.P_CHECK_EXCEPT_REALLOC, false);
		store.setDefault(IPreferenceConstants.P_USE_INLINE_SUPPRESSIONS, false);
		store.setDefault(IPreferenceConstants.P_CHECK_FORCE, false);
		store.setDefault(IPreferenceConstants.P_CHECK_UNUSED_FUNCTIONS, false);
		store.setDefault(IPreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES, false);
		store.setDefault(IPreferenceConstants.P_FOLLOW_USER_INCLUDES, false);
		store.setDefault(IPreferenceConstants.P_NUMBER_OF_THREADS, 1);
	}
	
	private static void initializeProblemsDefault(IPreferenceStore store) {
		
	}
	
	public static void initializePropertiesDefault(IPreferenceStore store) {
		store.setDefault(IPreferenceConstants.P_ADVANCED_ARGUMENTS, "");
		store.setDefault(IPreferenceConstants.P_RUN_ON_BUILD, false);
		store.setDefault(IPreferenceConstants.PROBLEMS_PAGE_ID + IPreferenceConstants.P_USE_PARENT_SUFFIX, true);
		store.setDefault(IPreferenceConstants.SETTINGS_PAGE_ID + IPreferenceConstants.P_USE_PARENT_SUFFIX, true);
		
		initializeSettingsDefault(store);
		initializeProblemsDefault(store);
	}
	
	private static void initializeBinaryPathPreferencesDefault(IPreferenceStore store) {
		store.setDefault(IPreferenceConstants.P_AUTOMATIC_UPDATE_CHECK_INTERVAL, "weekly");
		store.setDefault(IPreferenceConstants.P_USE_AUTOMATIC_UPDATE_CHECK, true);
	}
}
