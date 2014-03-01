package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;

/**
 * This class is used for WorkspacePreference as well as for ProjectProperties
 * @author Konrad Windszus
 *
 */
public class ProblemsPreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {

	
	public ProblemsPreferencePage() {
		super(GRID, true);
		// this is overridden in case of project properties
		setPreferenceStore(CppcheclipsePlugin.getWorkspacePreferenceStore());
		setDescription(Messages.ProblemsPreferencePage_Description);
	}

	@Override
	protected void createFieldEditors() {
	
		ProblemsTreeEditor problemsTreeEditor = new ProblemsTreeEditor(
				getFieldEditorParent());
		addField(problemsTreeEditor);
	}

	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.FieldEditorOverlayPage#getPageId()
	 */
	@Override
	protected String getPageId() {
		return IPreferenceConstants.PROBLEMS_PAGE_ID;
	}
	
	
}
