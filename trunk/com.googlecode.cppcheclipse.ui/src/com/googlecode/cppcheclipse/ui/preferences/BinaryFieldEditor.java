package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

import com.googlecode.cppcheclipse.command.VersionCommand;
import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;

public class BinaryFieldEditor extends FileFieldEditor {

	public static interface IBinaryChangeListener {
		public void binaryChange(String binaryPath);
	}
	
	private IPreferenceStore preference;
	private IBinaryChangeListener listener;
	private String lastBinaryPath;

	public BinaryFieldEditor(String name, String labelText,
			boolean enforceAbsolute, int validationStrategy, Composite parent, IPreferenceStore preference) {
		super(name, labelText, enforceAbsolute, validationStrategy, parent);
		this.preference = preference;
		this.listener = null;
		super.setPreferenceStore(preference);
	}

	@Override
	protected boolean checkState() {
		if (!super.checkState()) {
			lastBinaryPath = "";
			return false;
		}
			
		// check if it is valid cppcheck binary
		try {
			String path = getTextControl().getText();
			VersionCommand versionCommand = new VersionCommand();
			versionCommand.setBinaryPath(path);
			versionCommand.run();
			
			if (lastBinaryPath != null && !path.equals(lastBinaryPath)) {
				if (listener != null) {
					listener.binaryChange(path);
				}
			}
			
			// update profile!
			return true;
		} catch (Exception e) {
			CppcheclipsePlugin.log(e);
		}
		return false;
	}

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		// only overwrite preference store, if it was not set with the constructor
		if (this.preference == null || store == null) {
			super.setPreferenceStore(store);
		}
	}
	
	public void setBinaryChangeListener(IBinaryChangeListener listener) {
		this.listener = listener;
	}
	

}
