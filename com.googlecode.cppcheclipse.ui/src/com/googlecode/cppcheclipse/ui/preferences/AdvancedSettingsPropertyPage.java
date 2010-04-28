package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;

public class AdvancedSettingsPropertyPage extends FieldEditorOverlayPage
		implements IWorkbenchPropertyPage {

	public AdvancedSettingsPropertyPage() {
		super(FLAT, false);
		setDescription(Messages.AdvancedSettingsPropertyPage_Description);
	}
	
	@Override
	protected void createFieldEditors() {
		StringFieldEditor advancedArguments = new StringFieldEditor(IPreferenceConstants.P_ADVANCED_ARGUMENTS, Messages.AdvancedSettingsPropertyPage_AdvancedArguments, getFieldEditorParent());
		addField(advancedArguments);
	}	

	@Override
	/**
	 * not necessary for pure property pages
	 */
	protected String getPageId() {
		return null;
	}
	
}
