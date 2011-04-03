package com.googlecode.cppcheclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.googlecode.cppcheclipse.core.utils.SerializeHelper;

public class Appendages implements TableModel<File> {
	private final IPreferenceStore preferenceStore;
	private static final String DELIMITER = ";"; // something which does not
													// occur within base64
													// encoding
	private final List<File> files;

	public Appendages(IPreferenceStore preferenceStore) {
		this.preferenceStore = preferenceStore;
		files = new LinkedList<File>();
		load();
	}

	private void load() {
		String[] values = StringUtils.split(preferenceStore
				.getString(IPreferenceConstants.P_APPENDAGES), DELIMITER);
		for (String file : values) {
			try {
				files.add((File) SerializeHelper.fromString(file));
			} catch (Exception e) {
				CppcheclipsePlugin.logWarning("Error reading filename of appendage", e);
			}
		}
	}

	public void save() throws IOException {
		List<String> values = new LinkedList<String>();
		// serialize files
		for (File file : files) {
			values.add(SerializeHelper.toString(file));
		}
		preferenceStore.setValue(IPreferenceConstants.P_APPENDAGES, StringUtils
				.join(values, DELIMITER));
	}
	
	public void removeAll() {
		files.clear();
	}
	
	public void add(File file) {
		files.add(file);
	}
	
	public void remove(File file) {
		files.remove(file);
	}

	public Iterator<File> iterator() {
		return files.iterator();
	}
	
	public boolean isEmpty() {
		return files.isEmpty();
	}
	
	public File[] toArray() {
		return files.toArray(new File[files.size()]);
	}
}
