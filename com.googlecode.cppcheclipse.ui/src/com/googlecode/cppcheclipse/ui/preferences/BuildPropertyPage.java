package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.actions.Nature;

public class BuildPropertyPage extends FieldEditorPreferencePage implements
		IWorkbenchPropertyPage {
	private IAdaptable element;

	private BooleanFieldEditor runOnBuild;
	
	public BuildPropertyPage() {
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
		runOnBuild = new BooleanFieldEditor(PreferenceConstants.P_RUN_ON_BUILD,
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime
	 * .IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		this.element = element;
		setPreferenceStore(CppcheclipsePlugin.getProjectPreferenceStore((IProject) getElement(), false));
	}
}
