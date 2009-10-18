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
		store.setDefault(PreferenceConstants.P_RUN_ON_BUILD, false);
		store.setDefault(PreferenceConstants.P_USE_PARENT, true);
		store.setDefault(PreferenceConstants.P_CHECK_ALL, true);
		store.setDefault(PreferenceConstants.P_CHECK_STYLE, true);
		store.setDefault(PreferenceConstants.P_CHECK_UNUSED_FUNCTIONS, false);
		store.setDefault(PreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES, false);
		store.setDefault(PreferenceConstants.P_NUMBER_OF_THREADS, 1);
	}

}
