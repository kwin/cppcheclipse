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

/**
 * Constant definitions for plug-in preferences
 */
public interface PreferenceConstants {
	public static final String P_RUN_ON_BUILD = "runOnBuild";
	public static final String P_PROBLEMS = "problems";
	public static final String P_USE_PARENT_SUFFIX = "_useParentScope";
	public static final String P_BINARY_PATH = "binaryPath";
	public static final String P_CHECK_STYLE = "checkStyle";
	public static final String P_CHECK_ALL = "checkAll";
	public static final String P_CHECK_VERBOSE = "checkVerbose";
	public static final String P_CHECK_FORCE = "checkForce";
	public static final String P_CHECK_UNUSED_FUNCTIONS = "checkUnusedFunctions";
	public static final String P_FOLLOW_SYSTEM_INCLUDES = "followSystemIncludes";
	public static final String P_NUMBER_OF_THREADS = "numberOfThreads";
	public static final String P_AUTOMATIC_UPDATE_CHECK_INTERVAL = "automaticUpdateCheckInterval";
	public static final String P_USE_AUTOMATIC_UPDATE_CHECK = "automaticUpdateCheck";
	public static final String P_LAST_UPDATE_CHECK = "lastUpdateCheck";
	public static final String P_SUPPRESSIONS = "suppressions";
	
	// these are the page id's of the preferences but also the prefixes for the use parent properties
	public static final String PROBLEMS_PAGE_ID = "com.googlecode.cppcheclipse.ui.ProblemsPreferencePage";
	public static final String SETTINGS_PAGE_ID = "com.googlecode.cppcheclipse.ui.SettingsPreferencePage";
}
