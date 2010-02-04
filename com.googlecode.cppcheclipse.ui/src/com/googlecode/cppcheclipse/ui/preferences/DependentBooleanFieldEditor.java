package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class DependentBooleanFieldEditor extends BooleanFieldEditor {

	private final BooleanFieldEditor dependencyFieldEditor;
	
	public DependentBooleanFieldEditor(BooleanFieldEditor dependencyFieldEditor) {
		super();
		this.dependencyFieldEditor = dependencyFieldEditor;
	}

	public DependentBooleanFieldEditor(BooleanFieldEditor dependencyFieldEditor, String name, String label,
			Composite parent) {
		super(name, label, parent);
		this.dependencyFieldEditor = dependencyFieldEditor;
	}

	public DependentBooleanFieldEditor(BooleanFieldEditor dependencyFieldEditor, String name, String labelText,
			int style, Composite parent) {
		super(name, labelText, style, parent);
		this.dependencyFieldEditor = dependencyFieldEditor;
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		// only enable this field if dependency field editor is not enabled
		if (enabled) {
			// although the getBooleanValue is not necessary set for initialization (depends on order)
			// this is necessary for switching from workspace settings to project settings
			enabled = !dependencyFieldEditor.getBooleanValue();
		}
		super.setEnabled(enabled, parent);
	}
}
