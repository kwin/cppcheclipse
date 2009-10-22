package com.googlecode.cppcheclipse.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.googlecode.cppcheclipse.command.ErrorListCommand;

/**
 * Maintains all problems, and give back a current profile, with the valid
 * checks
 * 
 * @author Konrad Windszus
 * 
 */
public class ProblemProfile implements Cloneable {

	private Map<String, Problem> problems;
	private IPropertyChangeListener binaryChangeListener;

	public ProblemProfile(IPreferenceStore configurationPreferences, String binaryPath) {
		initProfileProblems(binaryPath);

		// register change listener for binary path
		CppcheclipsePlugin.getConfigurationPreferenceStore()
				.addPropertyChangeListener(new IPropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent event) {
						if (PreferenceConstants.P_BINARY_PATH.equals(event
								.getProperty())) {
							initProfileProblems(null);
							if (binaryChangeListener != null) {
								binaryChangeListener.propertyChange(event);
							}
						}
					}
				});
	}

	private void initProfileProblems(String binaryPath) {
		problems = new HashMap<String, Problem>();
		try {
			ErrorListCommand command = new ErrorListCommand();
			if (binaryPath != null) {
				command.setBinaryPath(binaryPath);
			}
			Collection<Problem> problemList = command.run();
			for (Problem problem : problemList) {
				if (problems.put(problem.getId(), problem) != null) {
					CppcheclipsePlugin.log("Found duplicate id: " + problem.getId());
				}
			}
			
			// convert the collection to a hashmap
		} catch (Exception e) {
			CppcheclipsePlugin.log(e);
			
		}
	}

	public void addBinaryChangeListener(
			IPropertyChangeListener binaryChangeListener) {
		this.binaryChangeListener = binaryChangeListener;
	}

	public void loadFromPreferences(IPreferenceStore preferences) {
		try {
			for (Problem problem : problems.values()) {
				String serialization = preferences
						.getString(PreferenceConstants.P_PROBLEMS_PREFIX
								+ problem.getId());
				if (!IPreferenceStore.STRING_DEFAULT_DEFAULT
						.equals(serialization)) {
					problem.deserializeNonFinalFields(serialization);
				}
			}
		} catch (RuntimeException e) {
			CppcheclipsePlugin.showError(
					"Invalid problem profile preferences found", e);
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		ProblemProfile profile = (ProblemProfile) super.clone();
		if (problems instanceof Cloneable) {

			// copy all problems
			profile.problems = new HashMap<String, Problem>();
			for (Problem problem : problems.values()) {
				profile.problems
						.put(problem.getId(), (Problem) problem.clone());
			}
		}
		return profile;
	}

	public void saveToPreferences(IPreferenceStore preferences) {
		for (Problem problem : problems.values()) {
			String serialization = problem.serializeNonFinalFields();
			preferences.setValue(PreferenceConstants.P_PROBLEMS_PREFIX
					+ problem.getId(), serialization);
		}
	}

	public Collection<String> getCategories() {
		Collection<String> categories = new LinkedList<String>();
		for (Problem problem : problems.values()) {
			String category = problem.getCategory();
			if (!categories.contains(category)) {
				categories.add(category);
			}
		}
		return categories;
	}

	public Map<String, Problem> getProblemsOfCategory(String category) {
		Map<String, Problem> problems = new HashMap<String, Problem>();
		for (Problem problem : this.problems.values()) {
			if (category.equals(problem.getCategory())) {
				problems.put(problem.getId(), problem);
			}
		}
		return problems;
	}

	public void reportProblems(Collection<Problem> problems)
			throws CoreException {
		for (Problem problem : problems) {
			if (isProblemEnabled(problem)) {
				problem.report();
			}
		}
	}

	public boolean isProblemEnabled(Problem problem) {
		// problems.
		// find problem in profile
		Problem problemInProfile = problems.get(problem.getId());
		if (problemInProfile != null) {
			boolean isEnabled = problemInProfile.isEnabled();

			// also overwrite the severity
			if (isEnabled) {
				problem.setSeverity(problemInProfile.getSeverity());
			}
			return isEnabled;
		}
		return true;
	}
}
