package com.googlecode.cppcheclipse.ui.preferences;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.ui.Messages;

public class SettingsPreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {
	
	private BooleanFieldEditor allCheck;
	private BooleanFieldEditor unusedFunctionsCheck;
	private IntegerFieldEditor numberOfThreads;
	private List<BooleanFieldEditor> checkEditors;
	private Group group;

	public SettingsPreferencePage() {
		super(GRID);
		// this is overridden in case of project properties
		setPreferenceStore(CppcheclipsePlugin.getWorkspacePreferenceStore());
		setDescription(Messages.SettingsPreferencePage_Description);
	}

	@Override
	protected String getPageId() {
		return IPreferenceConstants.SETTINGS_PAGE_ID;
	}

	@Override
	protected void createFieldEditors() {

		numberOfThreads = new IntegerFieldEditor(
				IPreferenceConstants.P_NUMBER_OF_THREADS,
				Messages.SettingsPreferencePage_NumberOfThreads, getFieldEditorParent(), 2);
		numberOfThreads.setValidRange(1, 16);
		addField(numberOfThreads);

		// available checks
		final Composite parent = getFieldEditorParent();
		beforeControlInsertion(parent);
		group = new Group(parent, SWT.NONE);
		group.setText("Checks (--enable=<check>)");
		
		checkEditors = new LinkedList<BooleanFieldEditor>();
		
		allCheck = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_ALL, Messages.SettingsPreferencePage_CheckAll,
				group) {
			
			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				super.valueChanged(oldValue, newValue);
				
				// enabling also depends on unusedFunctions
				if (!newValue) {
					numberOfThreads.setEnabled(!newValue && !unusedFunctionsCheck.getBooleanValue(),
							getFieldEditorParent());
				} else {
					numberOfThreads.setEnabled(!newValue,
							getFieldEditorParent());
				}
				for (BooleanFieldEditor checkEditor : checkEditors) {
					checkEditor.setEnabled(!newValue,
						group);
				}
			}
		};
		addField(allCheck);
		
		BooleanFieldEditor checkEditor =  new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_STYLE, Messages.SettingsPreferencePage_CheckStyle,
				group);
		addField(checkEditor);
		checkEditors.add(checkEditor);
		
		checkEditor = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_POSSIBLE_ERROR, Messages.SettingsPreferencePage_CheckPossibleError,
				group);
		addField(checkEditor);
		checkEditors.add(checkEditor);
		
		// disable thread handling in case of unused function check is enabled
		unusedFunctionsCheck = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_UNUSED_FUNCTIONS,
				Messages.SettingsPreferencePage_UnusedFunctions,
				group) {

			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				numberOfThreads.setEnabled(!newValue,
							getFieldEditorParent());
				super.valueChanged(oldValue, newValue);
			}
		};
		addField(unusedFunctionsCheck);
		checkEditors.add(unusedFunctionsCheck);
		
		checkEditor = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_EXCEPT_NEW, Messages.SettingsPreferencePage_CheckExceptionInNew,
				group);
		addField(checkEditor);
		checkEditors.add(checkEditor);
		
		checkEditor = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_EXCEPT_REALLOC, Messages.SettingsPreferencePage_CheckExceptionInRealloc,
				group);
		addField(checkEditor);
		checkEditors.add(checkEditor);
		
		Point size = group.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		group.setSize(size);
		
		group.setFont(parent.getFont());
		afterControlInsertion(group);
		
		// special flags
		final BooleanFieldEditor forceCheck = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_FORCE, Messages.SettingsPreferencePage_Force,
				getFieldEditorParent());
		addField(forceCheck);
		
		final BooleanFieldEditor verboseCheck = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_VERBOSE, Messages.SettingsPreferencePage_Verbose,
				getFieldEditorParent());
		addField(verboseCheck);
		
		final BooleanFieldEditor useInlineSuppressions = new BooleanFieldEditor(
				IPreferenceConstants.P_USE_INLINE_SUPPRESSIONS, Messages.SettingsPreferencePage_InlineSuppressions,
				getFieldEditorParent());
		addField(useInlineSuppressions);

		final BooleanFieldEditor debugCheck = new BooleanFieldEditor(
				IPreferenceConstants.P_CHECK_DEBUG, Messages.SettingsPreferencePage_Debug,
				getFieldEditorParent());
		addField(debugCheck);
	
		// TODO: enable when bug 878 of cppcheck is solved, see http://sourceforge.net/apps/trac/cppcheck/ticket/878
		/*
		final BooleanFieldEditor followSystemIncludes = new BooleanFieldEditor(
				PreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES,
				Messages.SettingsPreferencePage_FollowSystemIncludes,
				getFieldEditorParent());
		addField(followSystemIncludes);
		*/
	}	

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		refresh();
		// after that all controls are initialized
		return control;
	}

	public void init(IWorkbench workbench) {
	}
	
	private void beforeControlInsertion(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 8;
		parent.setLayout(layout);
	}

	private void afterControlInsertion(Control control) {
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		control.setLayoutData(gd);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		refresh();
	}
	
	private void refresh() {
		numberOfThreads.setEnabled(!unusedFunctionsCheck.getBooleanValue() && !allCheck.getBooleanValue(), getFieldEditorParent());
		for (BooleanFieldEditor editor : checkEditors) {
			editor.setEnabled(!allCheck.getBooleanValue(), group);
		}
	}
}
