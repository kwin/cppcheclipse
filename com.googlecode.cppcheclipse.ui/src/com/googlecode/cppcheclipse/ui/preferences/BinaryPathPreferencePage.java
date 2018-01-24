package com.googlecode.cppcheclipse.ui.preferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.core.command.Version;
import com.googlecode.cppcheclipse.core.command.VersionCommand;
import com.googlecode.cppcheclipse.core.utils.PathMacroReplacer;
import com.googlecode.cppcheclipse.ui.Console;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.UpdateCheck;
import com.googlecode.cppcheclipse.ui.Utils;

/**
 * Provides preferences for setting the binary path. We can't derive from
 * FieldEditorPreferences since we also use non-FieldEditor controls.
 * 
 * @see "http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html"
 * 
 * @author Konrad Windszus
 * 
 */
public class BinaryPathPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	private RadioGroupFieldEditor updateInterval;
	private Composite updateIntervalParent;
	private BooleanFieldEditor automaticUpdateCheck;
	private StringButtonFieldEditor binaryPath;
	private Link updateCheckNotice;
	private boolean hasBinaryPathChanged;
	private Link link;
	private String currentVersion = Messages.BinaryPathPreferencePage_UnknownVersion;

	public static final String PAGE_ID = "com.googlecode.cppcheclipse.ui.BinaryPathPreferencePage"; //$NON-NLS-1$
	private static final String PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_35 = "org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdatesPreferencePage"; //$NON-NLS-1$
	private static final String PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_34 = "org.eclipse.equinox.internal.p2.ui.sdk.AutomaticUpdatesPreferencePage"; //$NON-NLS-1$

	public void init(IWorkbench workbench) {
	}

	public BinaryPathPreferencePage() {
		super(FLAT);
		// this is overridden in case of project properties
		setPreferenceStore(CppcheclipsePlugin.getConfigurationPreferenceStore());
		setDescription(Messages.BinaryPathPreferencePage_Description);

		hasBinaryPathChanged = false;

	}

	private void setLastUpdateCheckDate() {
		Date lastUpdateCheckDate = UpdateCheck.getLastUpdateCheckDate();
		String lastUpdateCheck;
		if (lastUpdateCheckDate != null) {
			DateFormat format = new SimpleDateFormat();
			lastUpdateCheck = format.format(lastUpdateCheckDate);
		} else {
			lastUpdateCheck = Messages.BinaryPathPreferencePage_UpdateCheckNever;
		}

		// get version
		updateCheckNotice.setText(Messages.bind(
				Messages.BinaryPathPreferencePage_UpdateCheckNotice, lastUpdateCheck));
		Point size = updateCheckNotice.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		updateCheckNotice.setSize(size);
	}

	private void setCurrentVersion() {
		link.setText(Messages.bind(Messages.BinaryPathPreferencePage_LinkToCppcheck, currentVersion));
		Point size = link.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		link.setSize(size);
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		
		final FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		fileDialog.setText(Messages.BinaryPathPreferencePage_FileDialogTitle);
		
		binaryPath = new StringButtonFieldEditor(IPreferenceConstants.P_BINARY_PATH,
				Messages.BinaryPathPreferencePage_PathToBinary, parent) {

			@Override
			protected boolean checkState() {
				boolean result = false;
				currentVersion = Messages.BinaryPathPreferencePage_UnknownVersion;
				if (super.checkState()) {
					// check if it is valid cppcheck binary
					try {
						String path = PathMacroReplacer.performMacroSubstitution(getTextControl().getText());					        
						VersionCommand versionCommand = new VersionCommand(
								Console.getInstance(), path);
						Version version = versionCommand
								.run(new NullProgressMonitor());
						currentVersion = version.toString();
						// check for minimal required version
						if (!version.isCompatible()) {
							showErrorMessage(Messages
									.bind(
											Messages.BinaryPathPreferencePage_VersionTooOld,
											Version.MIN_VERSION, version));
						} else {
							result = true;
						}
					} catch (Exception e) {
						CppcheclipsePlugin.logError("Incompatible version", e);
						showErrorMessage();
					}
				}
				setCurrentVersion();
				return result;
			}
			
			

			@Override
			protected void fireValueChanged(String property, Object oldValue,
					Object newValue) {
				// is only called if old value isn't equal to new value
				hasBinaryPathChanged = true;
				super.fireValueChanged(property, oldValue, newValue);
			}



			@Override
			protected String changePressed() {
				// Browse button pressed, after selection finished, it replace the current text
				return fileDialog.open();
			}

		};
		binaryPath.setChangeButtonText(Messages.BinaryPathPreferencePage_FileDialogButton);
		binaryPath.setEmptyStringAllowed(false);
		binaryPath
				.setErrorMessage(Messages.BinaryPathPreferencePage_NoValidPath);
		addField(binaryPath);
		
		final StringVariableSelectionDialog variablesDialog = new StringVariableSelectionDialog(getShell());
		Button variablesButton = new Button(parent, SWT.PUSH | SWT.LEAD);
		variablesButton.setText(Messages.BinaryPathPreferencePage_VariablesButton);
		variablesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (variablesDialog.open() == IDialogConstants.OK_ID) {
					final String variable = variablesDialog.getVariableExpression();
					if (variable != null) {
						// append the selected variable to the end of the path
						binaryPath.setStringValue(binaryPath.getStringValue() + variable);
					}
				}
			}
		});
		
		afterControlInsertion(variablesButton);

		parent = getFieldEditorParent();
		beforeControlInsertion(parent);
		link = new Link(parent, SWT.NONE);
		setCurrentVersion();
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					Utils.openUrl(event.text);
				} catch (Exception e) {
					CppcheclipsePlugin.logError("Error opening link", e);
				}
			}
		});
		link.setFont(parent.getFont());
		afterControlInsertion(link);

		parent = getFieldEditorParent();
		automaticUpdateCheck = new BooleanFieldEditor(
				IPreferenceConstants.P_USE_AUTOMATIC_UPDATE_CHECK,
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
				IPreferenceConstants.P_AUTOMATIC_UPDATE_CHECK_INTERVAL,
				Messages.BinaryPathPreferencePage_UpdateInterval, 1,
				UpdateCheck.INTERVALS, updateIntervalParent);
		addField(updateInterval);

		parent = getFieldEditorParent();
		beforeControlInsertion(parent);
		Button updateCheckButton = new Button(parent, SWT.PUSH | SWT.LEAD);
		updateCheckButton
				.setText(Messages.BinaryPathPreferencePage_CheckForUpdate);
		updateCheckButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				UpdateCheck check = new UpdateCheck(false);
				try {
					Job job = check.check(binaryPath.getStringValue());
					job.join();
					setLastUpdateCheckDate();
				} catch (InterruptedException e) {
					CppcheclipsePlugin.logInfo("Update check interrupted!", e); //$NON-NLS-1$
				}
			}
		});
		afterControlInsertion(updateCheckButton);

		parent = getFieldEditorParent();
		updateCheckNotice = new Link(parent, SWT.NONE);
		setLastUpdateCheckDate();
		updateCheckNotice.addListener(SWT.Selection, new Listener() {
			@SuppressWarnings("unchecked")
			public void handleEvent(Event event) {
				try {
					// Create the dialog
					PreferenceManager preferenceManager = PlatformUI
							.getWorkbench().getPreferenceManager();
					List<IPreferenceNode> nodes = preferenceManager
							.getElements(PreferenceManager.POST_ORDER);
					String preferencePageId = ""; //$NON-NLS-1$
					for (IPreferenceNode node : nodes) {
						if (PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_34.equals(node
								.getId())
								|| PREFERENCE_PAGE_ID_AUTOMATIC_UPDATES_35
										.equals(node.getId())) {
							preferencePageId = node.getId();
							break;
						}
					}

					PreferenceDialog dialog = PreferencesUtil
							.createPreferenceDialogOn(getShell(),
									preferencePageId, null, null);
					dialog.open();
				} catch (Exception e) {
					CppcheclipsePlugin.logError("Error opening preference page", e);
				}
			}
		});
		updateCheckNotice.setFont(parent.getFont());
		afterControlInsertion(updateCheckNotice);
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

	@Override
	public boolean okToLeave() {
		if (!super.okToLeave())
			return false;

		if (hasBinaryPathChanged) {
			return askBeforeLeave();
		}
		return true;
	}

	private boolean askBeforeLeave() {
		String[] buttonLabels = { Messages.BinaryPathPreferencePage_ButtonSave,
				Messages.BinaryPathPreferencePage_ButtonDiscard,
				IDialogConstants.CANCEL_LABEL };
		MessageDialog messageDialog = new MessageDialog(getShell(),
				Messages.BinaryPathPreferencePage_AskBeforeLeaveTitle, null,
				Messages.BinaryPathPreferencePage_AskBeforeLeaveMessage,
				MessageDialog.QUESTION, buttonLabels, 0);
		int clickedButtonIndex = messageDialog.open();
		boolean okToLeave = false;
		switch (clickedButtonIndex) {
		case 0: // Save
			binaryPath.store();
			// we need to save all values here
			okToLeave = true;
			break;
		case 1: // Discard
			binaryPath.load();
			okToLeave = true;
		}
		if (okToLeave) {
			hasBinaryPathChanged = false;
		}
		return okToLeave;
	}
}
