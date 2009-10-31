package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;

public class SuppressionsPropertyPage extends FieldEditorPreferencePage
		implements IWorkbenchPropertyPage {
	private IAdaptable element;
	
	public SuppressionsPropertyPage() {
		super();
		setDescription(Messages.SuppressionsPropertyPage_Description);
	}

	@Override
	protected void createFieldEditors() {
		SuppressionsTable list = new SuppressionsTable(PreferenceConstants.P_SUPPRESSIONS, Messages.SuppressionsPropertyPage_SuppressionsLabel, getFieldEditorParent(), (IProject)getElement());
		addField(list);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		this.element = element;
		setPreferenceStore(CppcheclipsePlugin.getProjectPreferenceStore((IProject) getElement(), false));
	}

}
