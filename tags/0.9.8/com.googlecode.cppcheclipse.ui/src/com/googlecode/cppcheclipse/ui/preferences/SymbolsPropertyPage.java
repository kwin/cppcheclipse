package com.googlecode.cppcheclipse.ui.preferences;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPropertyPage;

import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.ToolchainSettings;
import com.googlecode.cppcheclipse.ui.Messages;

public class SymbolsPropertyPage extends FieldEditorOverlayPage implements
		IWorkbenchPropertyPage {
	private Collection <FieldEditor> restrictedSymbolEditors;
	private Group group;
	private SymbolsTable table;
	private ToolchainSettings settings;
	private BooleanFieldEditor restrictConfigurations;
	
	public SymbolsPropertyPage() {
		super(GRID, false);
		setDescription(Messages.SymbolsPropertyPage_Description);
		restrictedSymbolEditors = new LinkedList<FieldEditor>();
	}

	@Override
	protected void createFieldEditors() {
		settings = new ToolchainSettings(getProject());
		
		restrictConfigurations = new BooleanFieldEditor(IPreferenceConstants.P_RESTRICT_CONFIGURATION_CHECK, "Restrict checked configurations", getFieldEditorParent()) {
				@Override
				protected void valueChanged(boolean oldValue, boolean newValue) {
					super.valueChanged(oldValue, newValue);
					
					for (FieldEditor restrictedSymbolEditor : restrictedSymbolEditors) {
						restrictedSymbolEditor.setEnabled(newValue,
							group);
					}
				}
		};
		addField(restrictConfigurations);
		
		// available checks
		final Composite parent = getFieldEditorParent();
		group = new Group(parent, SWT.NONE);
		group.setText(Messages.SettingsPreferencePage_ChecksLabel);

		BooleanFieldEditor includeCDTSystemSymbols = new BooleanFieldEditor(IPreferenceConstants.P_INCLUDE_CDT_SYSTEM_SYMBOLS,
				"Include CDT System-Defined Symbols", group) {

					@Override
					protected void valueChanged(boolean oldValue,
							boolean newValue) {
						if (oldValue == newValue) {
							return;
						}
						if (newValue == false) {
							table.getModel().removeCDTSystemSymbols();
						} else {
							table.getModel().addSymbols(settings.getSystemSymbols());
						}
						table.getTableViewer().refresh();
						super.valueChanged(oldValue, newValue);
					}
			
		};
		restrictedSymbolEditors.add(includeCDTSystemSymbols);
		addField(includeCDTSystemSymbols, group);
		
		BooleanFieldEditor includeCDTUserSymbols = new BooleanFieldEditor(IPreferenceConstants.P_INCLUDE_CDT_USER_SYMBOLS,
				"Include CDT User-Defined Symbols", group) {

			@Override
			protected void valueChanged(boolean oldValue,
					boolean newValue) {
				if (oldValue == newValue) {
					return;
				}
				if (newValue == false) {
					table.getModel().removeCDTUserSymbols();
				} else {
					table.getModel().addSymbols(settings.getUserSymbols());
				}
				table.getTableViewer().refresh();
				super.valueChanged(oldValue, newValue);
			}
	
		};
		restrictedSymbolEditors.add(includeCDTUserSymbols);
		addField(includeCDTUserSymbols, group);
		
		table = new SymbolsTable(
				IPreferenceConstants.P_SYMBOLS,
				Messages.AppendagePropertyPage_AppendageLabel,
				group, settings);
		restrictedSymbolEditors.add(table);
		addField(table, group);
		
		// reset layout manager here, since every field editor reset the parent's layout manager in FieldEditor::createControl
		setCompositeLayout(group);
	}

	@Override
	protected String getPageId() {
		return null;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		refresh();
		// after that all controls are initialized
		return control;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		refresh();
	}
	
	private void refresh() {
		for (FieldEditor restrictedSymbolEditor : restrictedSymbolEditors) {
			restrictedSymbolEditor.setEnabled(restrictConfigurations.getBooleanValue(),
				group);
		}
	}

}
