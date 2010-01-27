package com.googlecode.cppcheclipse.ui.preferences;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.core.command.Version;
import com.googlecode.cppcheclipse.core.command.VersionCommand;
import com.googlecode.cppcheclipse.ui.Console;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.UpdateCheck;
import com.googlecode.cppcheclipse.ui.Utils;

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
	private FileFieldEditor binaryPath;
	private boolean hasBinaryPathChanged;

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

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		binaryPath = new FileFieldEditor(IPreferenceConstants.P_BINARY_PATH,
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
					Version version = versionCommand.run(new NullProgressMonitor());
					// check for minimal required version
					boolean isVersionCompatible = version.isGreaterOrEqual(Version.MIN_VERSION);
					if (!isVersionCompatible) {
						showErrorMessage(Messages.bind(Messages.BinaryPathPreferencePage_VersionTooOld, Version.MIN_VERSION, version));
						return false;
					}
					// update profile!
				} catch (Exception e) {
					CppcheclipsePlugin.log(e);
					showErrorMessage();
					return false;
				}
				return true;
			}

			@Override
			protected void valueChanged() {
				hasBinaryPathChanged = true;
				super.valueChanged();
			}

		};
		binaryPath.setEmptyStringAllowed(false);
		binaryPath
				.setErrorMessage(Messages.BinaryPathPreferencePage_NoValidPath);
		addField(binaryPath);

		parent = getFieldEditorParent();
		Link link = new Link(parent, SWT.NONE);
		link
				.setText(Messages.BinaryPathPreferencePage_LinkToCppcheck);
		Point size = link.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		link.setSize(size);
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					Utils.openUrl(event.text);
				} catch (Exception e) {
					CppcheclipsePlugin.log(e);
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
		link = new Link(parent, SWT.NONE);
		link
				.setText(Messages.BinaryPathPreferencePage_UpdateCheckNotice);
		size = link.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		link.setSize(size);
		link.addListener(SWT.Selection, new Listener() {
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
		String[] buttonLabels = { Messages.BinaryPathPreferencePage_ButtonSave, Messages.BinaryPathPreferencePage_ButtonDiscard,
				IDialogConstants.CANCEL_LABEL };
		MessageDialog messageDialog = new MessageDialog(
				getShell(),
				Messages.BinaryPathPreferencePage_AskBeforeLeaveTitle,
				null,
				Messages.BinaryPathPreferencePage_AskBeforeLeaveMessage,
				MessageDialog.QUESTION, buttonLabels, 0);
		int clickedButtonIndex = messageDialog.open();
		boolean okToLeave = false;
		switch (clickedButtonIndex) {
		case 0: // Save
			binaryPath.store();
			okToLeave = true;
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
