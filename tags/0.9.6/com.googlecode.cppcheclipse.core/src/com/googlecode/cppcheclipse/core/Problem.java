package com.googlecode.cppcheclipse.core;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class Problem implements Cloneable {
	private static final String DELIMITER = ";";

	private final String id, message, category;
	private final IResource resource;
	private final int lineNumber;
	private final File file; // either absolute or relative filename (to project), maybe null for problem profiles

	private boolean isEnabled;
	private ProblemSeverity severity; // a severity which can be used as
										// severity in IMarker
	private IProject project;
	
	/**
	 * Constructor is called for default problems (in problem profiles).
	 */
	public Problem(String id, String message, String category) {
		this(id, message, category, null, null, -1);
	}
	
	/**
	 * Constructor is called for specific problems in files.
	 * @param id
	 * @param message
	 * @param category
	 * @param filename
	 * @param project
	 * @param line
	 */
	public Problem(String id, String message, String category, File file, IProject project, int line) {
		this.id = id;
		this.message = message;
		this.category = category;
		if (file == null) {
			this.resource = null;
		} else {
			IResource resource = getResource(file, project);
			if (resource == null) {
				// if resource is outside project, the project is selected
				this.resource = project;
			} else {
				this.resource = resource;
			}
		}
		this.lineNumber = line;
		this.file = file;
		this.project = project;
		setToDefault();
	}
	
	private static IResource getResource(File filename, IProject project) {
		IResource resource = null;
		if (filename.isAbsolute()) {
			// find file in workspace (absolute path)
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath path = new Path(filename.toString());
			resource = root.getFileForLocation(path);
		} else {
			// find file in project (relative path)
			resource = project.getFile(filename.toString());
		}
		if (resource == null || !resource.exists()) {
			resource = null;
		}
		return resource;
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
	
	public IResource getResource() {
		return resource;
	}
	
	public File getFile() {
		return file;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isExternalFile() {
		return getResource() == project; 
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Problem other = (Problem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
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
