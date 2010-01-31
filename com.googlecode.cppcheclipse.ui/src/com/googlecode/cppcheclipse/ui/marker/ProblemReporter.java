package com.googlecode.cppcheclipse.ui.marker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.googlecode.cppcheclipse.core.IProblemReporter;
import com.googlecode.cppcheclipse.core.Problem;
import com.googlecode.cppcheclipse.ui.Messages;

public class ProblemReporter implements IProblemReporter {

	private static final String CHECKER_MARKER_TYPE = "com.googlecode.cppcheclipse.Problem"; //$NON-NLS-1$
	private List<IFile> checkedFiles;
	
	public ProblemReporter() {
		checkedFiles = new LinkedList<IFile>();
	}

	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.marker.IProblemReporter#reportProblem(com.googlecode.cppcheclipse.core.Problem)
	 */
	public void reportProblem(Problem problem) throws CoreException {
		StringBuffer message = new StringBuffer(problem.getMessage());
		int lineNumber = problem.getLineNumber();
		IFile file = problem.getFile();
		
		
		// check if this is really the file cppcheck meant
		String filename = problem.getFilename();
		if (!filename.equals(problem.getFile().getLocation().makeAbsolute().toOSString())) {
			// find file in workspace
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath path = new Path(filename);
			file = root.getFileForLocation(path);
			
			// if problem is outside the workspace, set marker to the file which included the other
			if (file == null) {
				file = problem.getFile();
				message.append(Messages.bind(Messages.ProblemReporter_ProblemInIncludedFile, filename, lineNumber));
				lineNumber = 0;
			} else {
				if (!checkedFiles.contains(file)) {
					deleteMarkers(file);
					checkedFiles.add(file);
				}
			}
		}
		final String completeMessage = Messages.bind(Messages.ProblemReporter_Message, problem.getCategory(), message);
		reportProblem(file, completeMessage, problem.getSeverity().intValue(), lineNumber, problem.getId());
	}
	
	
	@SuppressWarnings("unchecked")
	private void reportProblem(IFile file, String message, int severity, int lineNumber, String id) throws CoreException {
		// Do not put in duplicates
		IMarker[] cur = file.findMarkers(CHECKER_MARKER_TYPE,
				false, IResource.DEPTH_ZERO);
		if (cur != null) {
			for (IMarker element : cur) {
				int oldLineNumber = element.getAttribute(IMarker.LINE_NUMBER, 0);
				if (lineNumber == oldLineNumber) {
					String oldMessage = element.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
					int oldSeverity = element.getAttribute(IMarker.SEVERITY, 100);
					if (severity == oldSeverity
							&& message.equals(oldMessage))
						return;
				}
			}
		}
		
		// see http://wiki.eclipse.org/FAQ_Why_don%27t_my_markers_appear_in_the_editor%27s_vertical_ruler%3F
		Map attributes = new HashMap();
		MarkerUtilities.setLineNumber(attributes, lineNumber);
		MarkerUtilities.setMessage(attributes, message);
		attributes.put(IMarker.SEVERITY, severity);
		attributes.put(ID_ATTRIBUTE, id);
		MarkerUtilities.createMarker(file, attributes, CHECKER_MARKER_TYPE);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.marker.IProblemReporter#deleteMarkers(org.eclipse.core.resources.IResource)
	 */
	public void deleteMarkers(IResource file) throws CoreException {
		file.deleteMarkers(CHECKER_MARKER_TYPE, false, IResource.DEPTH_ZERO);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.marker.IProblemReporter#deleteAllMarkers()
	 */
	public void deleteAllMarkers() throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(
				CHECKER_MARKER_TYPE, false, IResource.DEPTH_INFINITE);

	}

	public void nextFile() {
		checkedFiles.clear();
	}
}
