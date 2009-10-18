package com.googlecode.cppcheclipse.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ProblemReporter {

	private static final String CHECKER_MARKER_TYPE = "com.googlecode.cppcheclipse.Problem";

	public static void reportProblem(Problem problem) throws CoreException {
		
		StringBuffer message = new StringBuffer();
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
				message.append("Problem in included file '").append(filename).append("' line ").append(lineNumber).append(": ");
				lineNumber = 0;
			}
		}
		
		message.append(problem.getCategory()).append(": ").append(problem.getMessage());
		reportProblem(problem.getFile(), message.toString(), problem.getSeverity().intValue(), lineNumber);
	}
	
	public static void reportProblem(IFile file, String message, int severity, int lineNumber) throws CoreException {
		// Do not put in duplicates
		IMarker[] cur = file.findMarkers(CHECKER_MARKER_TYPE,
				false, IResource.DEPTH_ZERO);
		if (cur != null) {
			for (IMarker element : cur) {
				int oldLineNumber = element.getAttribute(IMarker.LINE_NUMBER, 0);
				if (lineNumber == oldLineNumber) {
					String oldMessage = element.getAttribute(IMarker.MESSAGE, "");
					int oldSeverity = element.getAttribute(IMarker.SEVERITY, 100);
					if (severity == oldSeverity
							&& message.equals(oldMessage))
						return;
				}
			}
		}
		IMarker marker = file.createMarker(CHECKER_MARKER_TYPE);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
	}

	public static void deleteMarkers(IResource file) throws CoreException {
		file.deleteMarkers(CHECKER_MARKER_TYPE, false, IResource.DEPTH_ZERO);

	}

	public static void deleteAllMarkers() throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(
				CHECKER_MARKER_TYPE, false, IResource.DEPTH_INFINITE);

	}
}
