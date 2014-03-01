package com.googlecode.cppcheclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

public class SuppressionProfile implements TableModel<Suppression> {
	private static final String DELIMITER = "!";

	// contains the suppressions per file
	private final List<Suppression> suppressionList;
	private final IPreferenceStore projectPreferences;
	private final IProject project;

	public SuppressionProfile(IPreferenceStore projectPreferences,
			IProject project) {
		this.projectPreferences = projectPreferences;
		this.suppressionList = new LinkedList<Suppression>();
		this.project = project;
		load();
	}

	private void load() {
		String suppressions = projectPreferences
				.getString(IPreferenceConstants.P_SUPPRESSIONS);
		StringTokenizer tokenizer = new StringTokenizer(suppressions, DELIMITER);
		while (tokenizer.hasMoreTokens()) {
			try {
				Suppression suppression = Suppression.deserialize(tokenizer
						.nextToken());
				addSuppression(suppression);
			} catch (Exception e) {
				CppcheclipsePlugin.logWarning("Could not load preferences", e);
			}
		}
	}

	public void save() throws IOException {
		StringBuffer suppressions = new StringBuffer();
		for (Suppression suppression : suppressionList) {
			suppressions.append(suppression.serialize()).append(DELIMITER);
		}

		projectPreferences.setValue(IPreferenceConstants.P_SUPPRESSIONS,
				suppressions.toString());

		if (projectPreferences instanceof IPersistentPreferenceStore) {
			((IPersistentPreferenceStore) projectPreferences).save();
		}
	}

	private void addSuppression(Suppression suppression) {
		suppressionList.add(suppression);
	}

	public Suppression addFileSuppression(File file) {
		Suppression suppression = new Suppression(file, project);
		addSuppression(suppression);
		return suppression;
	}

	public void addProblemSuppression(File file, String problemId) {
		addSuppression(new Suppression(file, problemId));
	}

	public void addProblemInLineSuppression(File file, String problemId,
			int line) {
		addSuppression(new Suppression(file, problemId, line));
	}

	public void remove(Suppression suppression) {
		suppressionList.remove(suppression);
	}

	public void removeAll() {
		suppressionList.clear();
	}

	private File makeAbsoluteFile(File file) {
		if (file.isAbsolute()) {
			return file;
		}
		return new File(project.getLocation().toFile(), file.toString());
	}

	public boolean isFileSuppressed(File file) {
		File absoluteFile = makeAbsoluteFile(file);
		for (Suppression suppression : suppressionList) {
			if (suppression.isSuppression(absoluteFile, project)) {
				return true;
			}
		}
		return false;
	}

	public boolean isProblemInLineSuppressed(File file, String problemId,
			int line) {
		// not possible to suppress problems with no file attached
		if (file == null) {
			return false;
		}
		File absoluteFile = makeAbsoluteFile(file);
		for (Suppression suppression : suppressionList) {
			if (suppression.isSuppression(absoluteFile, problemId, line,
					project)) {
				return true;
			}
		}
		return false;
	}

	public final Collection<Suppression> getSuppressions() {
		return suppressionList;
	}

	public Iterator<Suppression> iterator() {
		return suppressionList.iterator();
	}

	public Suppression[] toArray() {
		return (Suppression[]) suppressionList.toArray(new Suppression[0]);
	}
}
