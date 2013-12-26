package com.googlecode.cppcheclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
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
		Iterable<String> values = Splitter
				.on(DELIMITER)
				.omitEmptyStrings()
				.split(preferenceStore
						.getString(IPreferenceConstants.P_APPENDAGES));
		for (String file : values) {
			try {
				files.add((File) SerializeHelper.fromString(file));
			} catch (Exception e) {
				CppcheclipsePlugin.logWarning(
						"Error reading filename of appendages. Stored appendage file '"
								+ file + "'", e);
			}
		}
	}

	public void save() throws IOException {
		List<String> values = new LinkedList<String>();
		// serialize files
		for (File file : files) {
			values.add(SerializeHelper.toString(file));
		}
		preferenceStore.setValue(IPreferenceConstants.P_APPENDAGES,
				Joiner.on(DELIMITER).join(values));
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
