package com.googlecode.cppcheclipse.ui.preferences;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;
import com.googlecode.cppcheclipse.core.command.VersionCommand;
import com.googlecode.cppcheclipse.ui.Console;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.UpdateCheck;

/**
 * Provides preferences for setting the binary path. We can't derive from
 * FieldEditorPreferences since we also use non-FieldEditor controls.
 * 
 * @see http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html
 * 
 * @author Konrad Windszus
 * 
 */
public class BinaryPathPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	private RadioGroupFieldEditor updateInterval;
	private Composite updateIntervalParent;
	private BooleanFieldEditor automaticUpdateCheck;

	private static final String PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_35 = "org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdatesPreferencePage";
	private static final String PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_34 = "org.eclipse.equinox.internal.p2.ui.sdk.AutomaticUpdatesPreferencePage";

	public void init(IWorkbench workbench) {
	}

	public BinaryPathPreferencePage() {
		super(FLAT);
		// this is overridden in case of project properties
		setPreferenceStore(CppcheclipsePlugin.getConfigurationPreferenceStore());
		setDescription(Messages.BinaryPathPreferencePage_Description);

	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		FileFieldEditor binaryPath = new FileFieldEditor(
				PreferenceConstants.P_BINARY_PATH,
				Messages.BinaryPathPreferencePage_PathToBinary, true,
				FileFieldEditor.VALIDATE_ON_KEY_STROKE, parent) {

			@Override
			protected boolean checkState() {

				if (!super.checkState()) {
					return false;
				}

				// check if it is valid cppcheck binary
				try {
					String path = getTextControl().getText();
					VersionCommand versionCommand = new VersionCommand(
							new Console());
					versionCommand.setBinaryPath(path);
					versionCommand.run(new NullProgressMonitor());

					// update profile!
					return true;
				} catch (Exception e) {
					CppcheclipsePlugin.log(e);
				}
				showErrorMessage();
				return false;
			}

		};
		binaryPath.setEmptyStringAllowed(false);
		binaryPath
				.setErrorMessage(Messages.BinaryPathPreferencePage_NoValidPath);
		addField(binaryPath);

		parent = getFieldEditorParent();
		automaticUpdateCheck = new BooleanFieldEditor(
				PreferenceConstants.P_USE_AUTOMATIC_UPDATE_CHECK,
				Messages.BinaryPathPreferencePage_AutomaticUpdateCheck, parent) {
			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				if (oldValue != newValue) {
					updateInterval.setEnabled(newValue, updateIntervalParent);
				}
				super.valueChanged(oldValue, newValue);
			}
		};
		addField(automaticUpdateCheck);

		updateIntervalParent = getFieldEditorParent();
		updateInterval = new RadioGroupFieldEditor(
				PreferenceConstants.P_AUTOMATIC_UPDATE_CHECK_INTERVAL,
				Messages.BinaryPathPreferencePage_UpdateInterval, 1,
				UpdateCheck.INTERVALS, updateIntervalParent);
		addField(updateInterval);

		parent = getFieldEditorParent();
		beforeControlInsertion(parent);
		Button updateCheckButton = new Button(parent, SWT.PUSH | SWT.LEFT);
		updateCheckButton
				.setText(Messages.BinaryPathPreferencePage_CheckForUpdate);
		updateCheckButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				{
					UpdateCheck check = new UpdateCheck(false);
					check.check();
				}
			}
		});
		afterControlInsertion(updateCheckButton);
		parent = getFieldEditorParent();
		Link link = new Link(parent, SWT.NONE);
		link
				.setText("This update check only checks for updates to cppcheck.\nTo automatically check for updates to cppcheclipse click <A>here</A>.");
		Point size = link.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		link.setSize(size);
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					
					// Create the dialog
					PreferenceManager preferenceManager = PlatformUI.getWorkbench()
							.getPreferenceManager();
					List<IPreferenceNode> nodes = preferenceManager.getElements(PreferenceManager.POST_ORDER);
					String preferencePageId = "";
					for (IPreferenceNode node : nodes) {
						if (PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_34.equals(node.getId()) || PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_35.equals(node.getId())) {
							preferencePageId = node.getId();
							break;
						}
					}
					
					PreferenceDialog dialog = PreferencesUtil
							.createPreferenceDialogOn(getShell(),
									preferencePageId, null,
									null);
					dialog.open();
					
				} catch (Exception e) {
					CppcheclipsePlugin.log(e);
				}
			}
		});
		link.setFont(parent.getFont());
		afterControlInsertion(link);

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
		gd.horizontalSpan = 1;
		control.setLayoutData(gd);
	}

	@Override
	protected void initialize() {
		super.initialize();

		if (!automaticUpdateCheck.getBooleanValue()) {
			updateInterval.setEnabled(false, updateIntervalParent);
		}
	}

}
