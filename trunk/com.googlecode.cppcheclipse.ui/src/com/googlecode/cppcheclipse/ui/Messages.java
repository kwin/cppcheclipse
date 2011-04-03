package com.googlecode.cppcheclipse.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.googlecode.cppcheclipse.ui.messages"; //$NON-NLS-1$
	public static String AppendagePropertyPage_AppendageLabel;
	public static String AppendagePropertyPage_Description;
	public static String AppendageTable_ColumnFile;
	public static String AppendageTable_FileSelection;
	public static String AppendageTable_FileSelectionMessage;
	public static String UpdateCheck_Daily, UpdateCheck_JobName,
			UpdateCheck_Monthly, UpdateCheck_NoUpdateMessage,
			UpdateCheck_NoUpdateTitle,
			UpdateCheck_UpdateMessage, UpdateCheck_UpdateTitle,
			UpdateCheck_Weekly;
	public static String OverlayPage_UseWorkspaceSettings,
			OverlayPage_UseProjectSettings,
			OverlayPage_ConfigureWorkspaceSettings;
	public static String BinaryPathPreferencePage_AskBeforeLeaveMessage;
	public static String BinaryPathPreferencePage_AskBeforeLeaveTitle;
	public static String BinaryPathPreferencePage_AutomaticUpdateCheck;
	public static String BinaryPathPreferencePage_ButtonDiscard;
	public static String BinaryPathPreferencePage_ButtonSave;
	public static String BinaryPathPreferencePage_CheckForUpdate;
	public static String BinaryPathPreferencePage_Description;
	public static String BinaryPathPreferencePage_NoValidPath;
	public static String BinaryPathPreferencePage_PathToBinary;
	public static String BinaryPathPreferencePage_UnknownVersion;
	public static String BinaryPathPreferencePage_UpdateCheckNever;
	public static String BinaryPathPreferencePage_UpdateCheckNotice;
	public static String BinaryPathPreferencePage_UpdateInterval;
	public static String BinaryPathPreferencePage_VersionTooOld;
	public static String BinaryPathPreferencePage_LinkToCppcheck;
	public static String Builder_IncrementalBuilderTask;
	public static String Builder_PathEmptyMessage;
	public static String Builder_PathEmptyTitle;
	public static String Builder_ResouceVisitorTask;
	public static String BuildPropertyPage_Description;
	public static String BuildPropertyPage_RunOnBuild;
	public static String Console_Title;
	public static String ProblemReporter_ProblemInExternalFile;
	public static String ProblemReporter_Message;
	public static String ProblemsPreferencePage_Description;
	public static String ProblemsTreeEditor_Name;
	public static String ProblemsTreeEditor_Problems;
	public static String ProblemsTreeEditor_Severity;
	public static String ProgressReporter_TaskName;
	public static String ReportBug_Label;
	public static String RunCodeAnalysis_Error;
	public static String RunCodeAnalysis_JobName;
	
	public static String ClearMarkers_Error;
	public static String ClearMarkers_JobName;
	
	public static String SettingsPreferencePage_CheckAll;
	public static String SettingsPreferencePage_CheckStyle;
	public static String SettingsPreferencePage_Description;
	public static String SettingsPreferencePage_FollowUserIncludes;
	public static String SettingsPreferencePage_FollowSystemIncludes;
	public static String SettingsPreferencePage_Force;
	public static String SettingsPreferencePage_NumberOfThreads;
	public static String SettingsPreferencePage_UnusedFunctions;
	public static String SettingsPreferencePage_Verbose;
	public static String SuppressFileResolution_Label;
	public static String CheckDescriptionResolution_Label;
	public static String SuppressionsPropertyPage_Description;
	public static String SuppressionsPropertyPage_SuppressionsLabel;
	public static String SuppressionsTable_AllLines;
	public static String SuppressionsTable_AllProblems;
	public static String SuppressionsTable_ColumnFilename;
	public static String SuppressionsTable_ColumnLine;
	public static String SuppressionsTable_ColumnProblem;
	public static String SuppressionsTable_FileSelection;
	public static String TableEditor_FileSelectionErrorExactlyOne;
	public static String TableEditor_FileSelectionErrorFile;
	public static String SuppressionsTable_FileSelectionMessage;
	public static String SuppressProblemInLineResolution_Label;
	public static String SuppressProblemResolution_Label;
	public static String TableEditor_Add;
	public static String TableEditor_Remove;
	public static String TableEditor_RemoveAll;
	public static String TableEditor_AddExternal;
	public static String UpdateCheck_NeverCheckAgain;

	public static String AdvancedSettingsPropertyPage_AdvancedArguments;
	public static String AdvancedSettingsPropertyPage_Description;
	public static String SettingsPreferencePage_Debug;
	public static String SettingsPreferencePage_CheckExceptionInRealloc;
	public static String SettingsPreferencePage_CheckExceptionInNew;
	public static String SettingsPreferencePage_ChecksLabel;
	public static String SettingsPreferencePage_InlineSuppressions;
	
	public static String SymbolsPropertyPage_Description;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
