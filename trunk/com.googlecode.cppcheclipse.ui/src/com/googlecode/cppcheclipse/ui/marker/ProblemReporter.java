package com.googlecode.cppcheclipse.ui.marker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.googlecode.cppcheclipse.core.IProblemReporter;
import com.googlecode.cppcheclipse.core.Problem;
import com.googlecode.cppcheclipse.ui.Messages;

public class ProblemReporter implements IProblemReporter {

	private static final String CHECKER_MARKER_TYPE = "com.googlecode.cppcheclipse.Problem"; //$NON-NLS-1$
	

	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.marker.IProblemReporter#reportProblem(com.googlecode.cppcheclipse.core.Problem)
	 */
	public void reportProblem(Problem problem) throws CoreException {
		
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
				message.append(Messages.bind(Messages.ProblemReporter_Message, filename, lineNumber)).append(Messages.ProblemReporter_Delimiter);
				lineNumber = 0;
			}
		}
		
		message.append(problem.getCategory()).append(Messages.ProblemReporter_Delimiter).append(problem.getMessage()); //$NON-NLS-1$
		reportProblem(file, message.toString(), problem.getSeverity().intValue(), lineNumber, problem.getId());
	}
	
	
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
		IMarker marker = file.createMarker(CHECKER_MARKER_TYPE);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		marker.setAttribute(ID_ATTRIBUTE, id);
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
}
