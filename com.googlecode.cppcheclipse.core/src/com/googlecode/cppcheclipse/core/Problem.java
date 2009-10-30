package com.googlecode.cppcheclipse.core;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

public class Problem implements Cloneable {
	private static final String DELIMITER = ";";

	private final String id, message, category;
	private final IFile file;
	private final int lineNumber;
	private final String filename;

	private boolean isEnabled;
	private ProblemSeverity severity; // one severity which can be used as
										// severity in IMarker
	
	public Problem(String id, String message, String category, IFile file, String filename, int line) {
		this.id = id;
		this.message = message;
		this.category = category;
		this.file = file;
		this.lineNumber = line;
		this.filename = filename;

		setToDefault();
	}
	
	public void setToDefault() {
		// standard values for non-final fields
		this.isEnabled = true;
		this.severity = ProblemSeverity.fromCategory(category);
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}
	
	public String getCategory() {
		return category;
	}

	public ProblemSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(ProblemSeverity severity) {
		this.severity = severity;
	}
	
	public IFile getFile() {
		return file;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj instanceof Problem) {
			// check only if id's are identical
			return this.id.equals(((Problem) obj).id);
		}
		return false;
	}

	public String serializeNonFinalFields() {
		StringBuffer serialization = new StringBuffer();
		serialization.append(isEnabled).append(DELIMITER);
		serialization.append(severity.ordinal()).append(DELIMITER);
		return serialization.toString();
	}

	public void deserializeNonFinalFields(String serialization)
			throws NoSuchElementException, IllegalArgumentException {
		StringTokenizer tokenizer = new StringTokenizer(serialization,
				DELIMITER);
		String token = tokenizer.nextToken();
		boolean isEnabled = Boolean.parseBoolean(token);
		token = tokenizer.nextToken();
		int severity = Integer.parseInt(token);
		// range check for severity
		if (severity < IMarker.SEVERITY_INFO
				|| severity > IMarker.SEVERITY_ERROR) {
			throw new IllegalArgumentException(
					"Found invalid severity value of "
							+ String.valueOf(severity));
		}
		this.isEnabled = isEnabled;
		this.severity = ProblemSeverity.values()[severity];
	}
}
