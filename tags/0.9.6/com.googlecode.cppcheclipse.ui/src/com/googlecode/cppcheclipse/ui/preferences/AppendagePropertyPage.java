package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;

public class AppendagePropertyPage extends FieldEditorOverlayPage
		implements IWorkbenchPropertyPage {
	public AppendagePropertyPage() {
		super(FLAT, false);
		setDescription(Messages.AppendagePropertyPage_Description);
	}

	@Override
	protected void createFieldEditors() {
		AppendageTable list = new AppendageTable(IPreferenceConstants.P_APPENDAGES, Messages.AppendagePropertyPage_AppendageLabel, getFieldEditorParent(), getProject());
		addField(list);
	}

	@Override
	protected String getPageId() {
		return null;
	}
	
}
