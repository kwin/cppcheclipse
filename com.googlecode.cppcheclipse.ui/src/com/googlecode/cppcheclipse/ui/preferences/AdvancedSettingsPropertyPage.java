package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;

public class AdvancedSettingsPropertyPage extends FieldEditorPreferencePage
		implements IWorkbenchPropertyPage {

	private IAdaptable element;
	
	public AdvancedSettingsPropertyPage() {
		setDescription("Only use this if you are experienced with the command line version of cppcheck.");
	}
	
	@Override
	protected void createFieldEditors() {
		StringFieldEditor advancedArguments = new StringFieldEditor(PreferenceConstants.P_ADVANCED_ARGUMENTS, "Advanced command line arguments", getFieldEditorParent());
		addField(advancedArguments);
	}	

	public IAdaptable getElement() {
		return element;
	}

	public void setElement(IAdaptable element) {
		this.element = element;
		setPreferenceStore(CppcheclipsePlugin.getProjectPreferenceStore((IProject) getElement(), false));
	}

}
