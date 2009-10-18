package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;

import com.googlecode.cppcheclipse.command.VersionCommand;
import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;

public class BinaryFieldEditor extends FileFieldEditor {

	public BinaryFieldEditor(String name, String labelText,
			boolean enforceAbsolute, Composite parent) {
		super(name, labelText, enforceAbsolute, parent);
	}

	public BinaryFieldEditor(String name, String labelText,
			boolean enforceAbsolute, int validationStrategy, Composite parent) {
		super(name, labelText, enforceAbsolute, validationStrategy, parent);
	}

	@Override
	protected boolean checkState() {
		if (!super.checkState()) {
			return false;
		}
			
		// check if it is valid cppcheck binary
		try {
			String path = getTextControl().getText();
			VersionCommand versionCommand = new VersionCommand();
			versionCommand.setBinaryPath(path);
			versionCommand.run();
			

			// update profile!
			return true;
		} catch (Exception e) {
			CppcheclipsePlugin.log(e);
		}
		return false;
	}
}
