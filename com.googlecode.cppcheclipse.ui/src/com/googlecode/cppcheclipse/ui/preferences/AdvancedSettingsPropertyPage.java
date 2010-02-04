package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;

public class AdvancedSettingsPropertyPage extends FieldEditorPreferencePage
		implements IWorkbenchPropertyPage {

	private IAdaptable element;
	
	public AdvancedSettingsPropertyPage() {
		setDescription(Messages.AdvancedSettingsPropertyPage_Description);
	}
	
	@Override
	protected void createFieldEditors() {
		StringFieldEditor advancedArguments = new StringFieldEditor(IPreferenceConstants.P_ADVANCED_ARGUMENTS, Messages.AdvancedSettingsPropertyPage_AdvancedArguments, getFieldEditorParent());
		addField(advancedArguments);
	}	

	public IAdaptable getElement() {
		return element;
	}

	public void setElement(IAdaptable element) {
		this.element = element;
		setPreferenceStore(CppcheclipsePlugin.getProjectPreferenceStore((IProject) getElement()));
	}

}
