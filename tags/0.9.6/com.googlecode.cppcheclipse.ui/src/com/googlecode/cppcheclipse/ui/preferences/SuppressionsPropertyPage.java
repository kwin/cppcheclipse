package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;

public class SuppressionsPropertyPage extends FieldEditorOverlayPage
		implements IWorkbenchPropertyPage {
	public SuppressionsPropertyPage() {
		super(FLAT, false);
		setDescription(Messages.SuppressionsPropertyPage_Description);
	}

	@Override
	protected void createFieldEditors() {
		SuppressionsTable list = new SuppressionsTable(IPreferenceConstants.P_SUPPRESSIONS, Messages.SuppressionsPropertyPage_SuppressionsLabel, getFieldEditorParent(), getProject());
		addField(list);
	}

	@Override
	protected String getPageId() {
		return null;
	}
	
}
