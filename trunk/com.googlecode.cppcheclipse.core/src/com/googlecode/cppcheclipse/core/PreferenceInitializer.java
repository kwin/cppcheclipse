/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
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
	
	public static void initializeSettingsDefault(IPreferenceStore store) {
		store.setDefault(PreferenceConstants.P_CHECK_ALL, true);
		store.setDefault(PreferenceConstants.P_CHECK_STYLE, true);
		store.setDefault(PreferenceConstants.P_CHECK_VERBOSE, false);
		store.setDefault(PreferenceConstants.P_CHECK_FORCE, false);
		store.setDefault(PreferenceConstants.P_CHECK_UNUSED_FUNCTIONS, false);
		store.setDefault(PreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES, false);
		store.setDefault(PreferenceConstants.P_NUMBER_OF_THREADS, 1);
	}
	
	public static void initializeProblemsDefault(IPreferenceStore store) {
		
	}
	
	public static void initializePropertiesDefault(IPreferenceStore store) {
		store.setDefault(PreferenceConstants.P_ADVANCED_ARGUMENTS, "");
		store.setDefault(PreferenceConstants.P_RUN_ON_BUILD, false);
		store.setDefault(PreferenceConstants.PROBLEMS_PAGE_ID + PreferenceConstants.P_USE_PARENT_SUFFIX, true);
		store.setDefault(PreferenceConstants.SETTINGS_PAGE_ID + PreferenceConstants.P_USE_PARENT_SUFFIX, true);
		
		initializeSettingsDefault(store);
		initializeProblemsDefault(store);
	}
	
	public static void initializeBinaryPathPreferencesDefault(IPreferenceStore store) {
		store.setDefault(PreferenceConstants.P_AUTOMATIC_UPDATE_CHECK_INTERVAL, "weekly");
		store.setDefault(PreferenceConstants.P_USE_AUTOMATIC_UPDATE_CHECK, true);
		
	}

}
