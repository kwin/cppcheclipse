package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.command.ErrorListCommand;

/**
 * Maintains all problems, and give back a current profile, with the valid
 * checks
 * 
 * @author kwindszus
 * 
 */
public class ProblemProfile {

	Map<String, Problem> problems;

	public ProblemProfile(IPreferenceStore preferenceStore) throws XPathExpressionException, IOException, InterruptedException, ParserConfigurationException, SAXException {
		this(null, preferenceStore);
	}
	
	public ProblemProfile(String binaryPath, IPreferenceStore preferenceStore) throws XPathExpressionException, IOException, InterruptedException, ParserConfigurationException, SAXException {
		ErrorListCommand command = new ErrorListCommand();
		if (binaryPath != null) {
			command.setBinaryPath(binaryPath);
		}
		problems = command.run();
		loadFromPreferences(preferenceStore);
	}
	
	public ProblemProfile() {
		this.problems = new HashMap<String, Problem>();
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
			CppcheclipsePlugin.showError("Invalid problem profile preferences found", e);
		}
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

	public void reportProblems(Map<String, Problem> problems) throws CoreException {
		for (Problem problem : problems.values()) {
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
			return problemInProfile.isEnabled();
		}
		return true;
	}
}
