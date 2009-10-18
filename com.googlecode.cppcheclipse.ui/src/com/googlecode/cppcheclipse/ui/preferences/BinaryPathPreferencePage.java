package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;

public class BinaryPathPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	public BinaryPathPreferencePage() {
		// this is overridden in case of project properties
		setPreferenceStore(CppcheclipsePlugin.getConfigurationPreferenceStore());
		setDescription("Path to cppcheck binary. You must get this binary separately from http://sourceforge.net/projects/cppcheck/.");
	}
	
	@Override
	protected void createFieldEditors() {
		BinaryFieldEditor binaryPath = new BinaryFieldEditor(
				PreferenceConstants.P_BINARY_PATH, 
				"cppcheck binary path", 
				true,
				FileFieldEditor.VALIDATE_ON_KEY_STROKE,
		 		getFieldEditorParent());
			binaryPath.setEmptyStringAllowed(false);
			binaryPath.setErrorMessage("No valid path to cppcheck specified");
			addField(binaryPath);
	}
}
