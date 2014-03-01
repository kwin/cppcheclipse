package com.googlecode.cppcheclipse.core;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public class Problem implements Cloneable {
	private static final String DELIMITER = ";";

	private final String id, message, category;
	private final int lineNumber;
	private final File file; // either absolute or relative filename (to
								// project), maybe null for problem profiles or
								// project related problems
	private boolean isEnabled;
	private ProblemSeverity severity; // a severity which can be used as
										// severity in IMarker
	private final IProject project;
	private Collection<IResource> resources;

	/**
	 * Constructor is called for default problems (in problem profiles).
	 */
	public Problem(String id, String message, String category) {
		this(id, message, category, null, null, -1);
	}

	/**
	 * Constructor is called for specific problems in files.
	 * 
	 * @param id
	 * @param message
	 * @param category
	 * @param filename
	 *            (might be null for non file-specific problems)
	 * @param project
	 * @param line
	 *            (might be 0 for non line-specific problems)
	 */
	public Problem(String id, String message, String category, File file,
			IProject project, int line) {
		this.id = id;
		this.message = message;
		this.category = category;
		this.lineNumber = line;
		this.file = file;
		this.project = project;
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

	/**
	 * 
	 * @return a collection of resources which are associated with this problem.
	 *         The collection might be empty.
	 */
	public Collection<IResource> getResources() {
		if (resources == null) {
			resources = new LinkedList<IResource>();
			if (file != null) {
				URI fileUri;
				if (!file.isAbsolute()) {
					// make file absolute and convert to URI (does not work the
					// other way round, because File.toURI always returns an
					// absolute URI
					fileUri = new File(new File(project.getLocationURI()
							.getPath()), file.toString()).toURI();
				} else {
					fileUri = file.toURI();
				}

				// find file in workspace (absolute path)
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

				// consider linked paths also
				IFile[] file = root.findFilesForLocationURI(fileUri);
				resources.addAll(Arrays.asList(file));
			}
			if (resources.isEmpty()) {
				// if no resource could be resolved take project
				resources.add(project);
			}
		}
		return resources;
	}

	public File getFile() {
		return file;
	}

	/**
	 * 
	 * @return a positive line number or 0 if there is no linenumber associated
	 *         with this problem
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isExternalFile() {
		// TODO: check
		return false;
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
