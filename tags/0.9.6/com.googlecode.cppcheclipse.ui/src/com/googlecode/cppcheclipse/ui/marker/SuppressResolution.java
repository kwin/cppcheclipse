package com.googlecode.cppcheclipse.ui.marker;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.SuppressionProfile;

public abstract class SuppressResolution implements IMarkerResolution {

	public abstract String getLabel();

	public void run(IMarker marker) {
		try {
			IResource resource = (IResource)marker.getResource();
			IProject project = resource.getProject();
			String problemId = marker.getAttribute(ProblemReporter.ATTRIBUTE_ID, ""); //$NON-NLS-1$
			int line = marker.getAttribute(ProblemReporter.ATTRIBUTE_ORIGINAL_LINE_NUMBER, 0);
			File file = new File(marker.getAttribute(ProblemReporter.ATTRIBUTE_FILE, "")); //$NON-NLS-1$
			marker.delete();
			SuppressionProfile profile = new SuppressionProfile(CppcheclipsePlugin.getProjectPreferenceStore(project), project);
			suppress(profile, resource, file, problemId, line);
			profile.save();
		} 
		catch (CoreException e) {
			CppcheclipsePlugin.log(e);
		} catch (IOException e) {
			CppcheclipsePlugin.log(e);
		}
	}

	protected abstract void suppress(SuppressionProfile profile, IResource resource, File file, String problemId, int line) throws CoreException;
}
