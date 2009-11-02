package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

@SuppressWarnings("unchecked")
public class SuppressionProfile {
	private static final String DELIMITER = "!";
	private MultiMap suppressionList; //this does not allow generics so far, contains IFile as key and Suppression as value
	private final IPreferenceStore projectPreferences;
	
	public SuppressionProfile(IPreferenceStore projectPreferences, IProject project) {
		this.projectPreferences = projectPreferences;
		this.suppressionList = new MultiValueMap();
		load(project);
	}
	
	private void load(IProject project) {
		try {
		String suppressions = projectPreferences.getString(PreferenceConstants.P_SUPPRESSIONS);
		StringTokenizer tokenizer = new StringTokenizer(suppressions, DELIMITER);
		while (tokenizer.hasMoreTokens()) {
			Suppression suppression = Suppression.deserialize(tokenizer.nextToken(), project);
			suppressionList.put(suppression.getFile(), suppression);
		}
		} catch (Exception e) {
			CppcheclipsePlugin.log(e);
		}
	}
	
	public void save() throws IOException {
		StringBuffer suppressions = new StringBuffer();
		Iterator iterator = suppressionList.values().iterator();
		while (iterator.hasNext()) {
			Suppression suppression  = (Suppression)iterator.next();
			suppressions.append(suppression.serialize()).append(DELIMITER);
		}
		
		projectPreferences.setValue(PreferenceConstants.P_SUPPRESSIONS, suppressions.toString());
		
		if (projectPreferences instanceof IPersistentPreferenceStore) {
			((IPersistentPreferenceStore) projectPreferences).save();
		}
	}
	
	public Suppression addFileSuppression(IFile file) {
		Suppression suppression =  new Suppression(file);
		suppressionList.put(file, suppression);
		return suppression;
	}
	
	public void addProblemSuppression(IFile file, String problemId) {
		suppressionList.put(file, new Suppression(file, problemId));
	}
	
	public void addProblemInLineSuppression(IFile file, String problemId, int line) {
		suppressionList.put(file, new Suppression(file, problemId, line));
	}
	
	public void removeSuppression(Suppression suppression) {
		suppressionList.remove(suppression.getFile(), suppression);
	}
	
	public void removeAllSuppression() {
		suppressionList.clear();
	}
	
	public boolean isFileSuppressed(IFile file) {
		Collection collection = (Collection) suppressionList.get(file);
		if (collection == null || collection.isEmpty())
			return false;

		Iterator iterator = collection.iterator();
		while (collection == null || iterator.hasNext()){
			Suppression suppression = (Suppression)iterator.next();
			if (suppression.isFileSuppression())
				return true;
		}
		return false;
	}
	
	public boolean isProblemInLineSuppressed(IFile file, String problemId, int line) {
		Collection collection = (Collection) suppressionList.get(file);
		if (collection == null || collection.isEmpty())
			return false;

		Iterator iterator = collection.iterator();
		while (iterator.hasNext()){
			Suppression suppression = (Suppression)iterator.next();
			if (suppression.isSuppression(file, problemId, line))
				return true;
		}
		return false;
	}
	
	public Collection getSuppressions() {
		return suppressionList.values();
	}
}
