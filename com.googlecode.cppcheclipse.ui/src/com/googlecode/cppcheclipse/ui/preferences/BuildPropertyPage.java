package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.Nature;

public class BuildPropertyPage extends FieldEditorOverlayPage implements
		IWorkbenchPropertyPage {
	private BooleanFieldEditor runOnBuild;
	
	public BuildPropertyPage() {
		super(FLAT, false);
		// must set preference store later
		runOnBuild = null;
		setDescription(Messages.BuildPropertyPage_Description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {
		runOnBuild = new BooleanFieldEditor(IPreferenceConstants.P_RUN_ON_BUILD,
				Messages.BuildPropertyPage_RunOnBuild, getFieldEditorParent());
		addField(runOnBuild);
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			if (runOnBuild != null) {
				Nature.toggleNature((IProject)getElement(), runOnBuild.getBooleanValue());
			}
		}
		return result;
	}

	@Override
	protected String getPageId() {
		return null;
	}
}
