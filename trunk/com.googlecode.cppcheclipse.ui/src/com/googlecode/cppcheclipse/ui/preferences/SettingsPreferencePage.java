package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;

public class SettingsPreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {
	
	private BooleanFieldEditor unusedFunctionsCheck;
	private IntegerFieldEditor numberOfThreads;

	public SettingsPreferencePage() {
		super(GRID);
		// this is overridden in case of project properties
		setPreferenceStore(CppcheclipsePlugin.getWorkspacePreferenceStore());
		setDescription("General settings of cppcheck.");
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.SETTINGS_PAGE_ID;
	}

	@Override
	protected void createFieldEditors() {

		numberOfThreads = new IntegerFieldEditor(
				PreferenceConstants.P_NUMBER_OF_THREADS,
				"Number of threads to use", getFieldEditorParent(), 2) {
			@Override
			public void setEnabled(boolean enabled, Composite parent) {
				// only enable if unused functions check is not set
				if (enabled) {
					enabled = !unusedFunctionsCheck.getBooleanValue();
				}
				super.setEnabled(enabled, parent);
			}
		};
		numberOfThreads.setValidRange(1, 16);
		addField(numberOfThreads);

		final BooleanFieldEditor styleCheck = new BooleanFieldEditor(
				PreferenceConstants.P_CHECK_STYLE, "Check style",
				getFieldEditorParent());
		addField(styleCheck);

		final BooleanFieldEditor allCheck = new BooleanFieldEditor(
				PreferenceConstants.P_CHECK_ALL, "Check all (--all)",
				getFieldEditorParent());
		addField(allCheck);

		unusedFunctionsCheck = new BooleanFieldEditor(
				PreferenceConstants.P_CHECK_UNUSED_FUNCTIONS,
				"Check unused functions (disables multiple thread processing)",
				getFieldEditorParent()) {

			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				if (oldValue != newValue) {
					numberOfThreads.setEnabled(!newValue,
							getFieldEditorParent());
				}
				super.valueChanged(oldValue, newValue);
			}
		};

		addField(unusedFunctionsCheck);

		// disable thread handling in case of unused function check is enabled
		// (PropertyChangeListenenr does not work with
		// FieldEditorPreferencePage)
		final BooleanFieldEditor followSystemIncludes = new BooleanFieldEditor(
				PreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES,
				"Follow system includes (may take very long)",
				getFieldEditorParent());
		addField(followSystemIncludes);
	}
	
	

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		numberOfThreads.setEnabled(!unusedFunctionsCheck.getBooleanValue(), getFieldEditorParent());
		// after that all controls are initialized
		return control;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}
}
