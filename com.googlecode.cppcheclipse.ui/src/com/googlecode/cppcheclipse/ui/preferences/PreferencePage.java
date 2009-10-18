package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;
import com.googlecode.cppcheclipse.core.ProblemProfile;

/**
 * This class is used for WorkspacePreference as well as for ProjectProperties
 * @author Konrad Windszus
 *
 */
public class PreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {

	private ProblemsTreeEditor problemsTreeEditor;
	
	public class BinaryChangeListener implements BinaryFieldEditor.IBinaryChangeListener {

		public void binaryChange(String binaryPath) {	
			// update profile
			try {
				ProblemProfile profile = new ProblemProfile(binaryPath, getPreferenceStore());
				problemsTreeEditor.loadProfile(profile);
			} catch (Exception e) {
				CppcheclipsePlugin.log(e);
			}
		}
	}
	
	public PreferencePage() {
		super(GRID);
		// this is overridden in case of project properties
		setPreferenceStore(CppcheclipsePlugin.getWorkspacePreferenceStore());
		setDescription("Preferences of cppcheclipse. You must get the cppcheck binary separately.");
	}

	@Override
	protected void createFieldEditors() {
		
		IPreferenceStore configurationStore = null;
		if (!isPropertyPage()) {
			configurationStore = CppcheclipsePlugin.getConfigurationPreferenceStore();
			BinaryFieldEditor binaryPath = new BinaryFieldEditor(
				PreferenceConstants.P_BINARY_PATH, 
				"cppcheck binary path", 
				true,
				FileFieldEditor.VALIDATE_ON_KEY_STROKE,
		 		getFieldEditorParent(),
		 		configurationStore);
			
			binaryPath.setBinaryChangeListener(new BinaryChangeListener());
			
			binaryPath.setErrorMessage("No valid path to cppcheck specified");
			addField(binaryPath);

		} else {
			
		}
		BooleanFieldEditor styleCheck = new BooleanFieldEditor(PreferenceConstants.P_CHECK_STYLE, "Check style", getFieldEditorParent());
		addField(styleCheck);
		BooleanFieldEditor allCheck = new BooleanFieldEditor(PreferenceConstants.P_CHECK_ALL, "Check all (--all)", getFieldEditorParent());
		addField(allCheck);
		BooleanFieldEditor unusedFunctionsCheck = new BooleanFieldEditor(PreferenceConstants.P_CHECK_UNUSED_FUNCTIONS, "Check unused functions", getFieldEditorParent());
		addField(unusedFunctionsCheck);
		BooleanFieldEditor followSystemIncludes = new BooleanFieldEditor(PreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES, "Follow system includes (may take very long)", getFieldEditorParent());
		addField(followSystemIncludes);
		IntegerFieldEditor numberOfThreads = new IntegerFieldEditor(PreferenceConstants.P_NUMBER_OF_THREADS, "Number of threads to use", getFieldEditorParent(), 2);
		numberOfThreads.setValidRange(1,16);
		addField(numberOfThreads);
		
	
		problemsTreeEditor = new ProblemsTreeEditor(
				getFieldEditorParent());
		addField(problemsTreeEditor);
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}
	

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.FieldEditorOverlayPage#getPageId()
	 */
	@Override
	protected String getPageId() {
		return "com.googlecode.cppcheclipse.ui.PreferencePage";
	}
}
