package com.googlecode.cppcheclipse.ui.marker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.googlecode.cppcheclipse.core.IProblemReporter;
import com.googlecode.cppcheclipse.core.Problem;
import com.googlecode.cppcheclipse.ui.Messages;

public class ProblemReporter implements IProblemReporter {

	private static final String CHECKER_MARKER_TYPE = "com.googlecode.cppcheclipse.Problem"; //$NON-NLS-1$

	// some additional attributes (which must be specified in plugin.xml as
	// well)
	public static final String ATTRIBUTE_ID = "problemId"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ORIGINAL_LINE_NUMBER = "originalLineNumber"; //$NON-NLS-1$
	public static final String ATTRIBUTE_FILE = "file"; //$NON-NLS-1$

	public ProblemReporter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.googlecode.cppcheclipse.ui.marker.IProblemReporter#reportProblem(
	 * com.googlecode.cppcheclipse.core.Problem)
	 */
	public void reportProblem(Problem problem) throws CoreException {
		final StringBuffer message = new StringBuffer();
		final int lineNumber;
		if (problem.isExternalFile()) {
			message.append(Messages.bind(
					Messages.ProblemReporter_ProblemInExternalFile, problem
							.getFile().toString(), problem.getLineNumber()));
			// lineNumber = 0;
		} else {

		}
		lineNumber = problem.getLineNumber();
		message.append(problem.getMessage());
		final String completeMessage = Messages.bind(
				Messages.ProblemReporter_Message, problem.getCategory(),
				message);
		reportProblem(problem.getResource(), completeMessage, problem
				.getSeverity().intValue(), lineNumber, problem.getId(), problem
				.getFile(), problem.getLineNumber());
	}

	
	private void reportProblem(IResource resource, String message,
			int severity, int lineNumber, String id, File file,
			int originalLineNumber) throws CoreException {
		// TODO: open external file, see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=151005 on how to
		// generate markers for external files

		
		// Do not put in duplicates
		IMarker[] cur = resource.findMarkers(CHECKER_MARKER_TYPE, false,
				IResource.DEPTH_ZERO);
		if (cur != null) {
			for (IMarker element : cur) {
				int oldLineNumber = element
						.getAttribute(IMarker.LINE_NUMBER, 0);
				if (lineNumber == oldLineNumber) {
					String oldMessage = element.getAttribute(IMarker.MESSAGE,
							""); //$NON-NLS-1$
					int oldSeverity = element.getAttribute(IMarker.SEVERITY,
							100);
					if (severity == oldSeverity && message.equals(oldMessage))
						return;
				}
			}
		}

		// see
		// http://wiki.eclipse.org/FAQ_Why_don%27t_my_markers_appear_in_the_editor%27s_vertical_ruler%3F
		Map<String, Object> attributes = new HashMap<String, Object>();
		MarkerUtilities.setLineNumber(attributes, lineNumber);
		MarkerUtilities.setMessage(attributes, message);
		attributes.put(IMarker.SEVERITY, severity);
		attributes.put(ATTRIBUTE_ID, id);
		attributes.put(ATTRIBUTE_FILE, file.toString());
		attributes.put(ATTRIBUTE_ORIGINAL_LINE_NUMBER, originalLineNumber);
		MarkerUtilities.createMarker(resource, attributes, CHECKER_MARKER_TYPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.googlecode.cppcheclipse.ui.marker.IProblemReporter#deleteMarkers(
	 * org.eclipse.core.resources.IResource)
	 */
	public void deleteMarkers(IResource file, boolean isRecursive)
			throws CoreException {
		final int depth;
		if (isRecursive) {
			depth = IResource.DEPTH_INFINITE;
		} else {
			depth = IResource.DEPTH_ZERO;
		}
		file.deleteMarkers(CHECKER_MARKER_TYPE, true, depth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.googlecode.cppcheclipse.ui.marker.IProblemReporter#deleteAllMarkers()
	 */
	public void deleteAllMarkers() throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(
				CHECKER_MARKER_TYPE, true, IResource.DEPTH_INFINITE);

	}
}
