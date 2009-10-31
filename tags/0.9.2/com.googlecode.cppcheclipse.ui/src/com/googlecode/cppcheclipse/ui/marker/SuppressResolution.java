package com.googlecode.cppcheclipse.ui.marker;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IProblemReporter;
import com.googlecode.cppcheclipse.core.SuppressionProfile;

public abstract class SuppressResolution implements IMarkerResolution {

	public abstract String getLabel();

	public void run(IMarker marker) {
		try {
			IFile file = (IFile)marker.getResource();
			IProject project = file.getProject();
			String problemId = marker.getAttribute(IProblemReporter.ID_ATTRIBUTE, ""); //$NON-NLS-1$
			int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
			marker.delete();
			SuppressionProfile profile = new SuppressionProfile(CppcheclipsePlugin.getProjectPreferenceStore(project, false), project);
			suppress(profile, file, problemId, line);
			profile.save();
		} 
		catch (CoreException e) {
			CppcheclipsePlugin.log(e);
		} catch (IOException e) {
			CppcheclipsePlugin.log(e);
		}
	}

	protected abstract void suppress(SuppressionProfile profile, IFile file, String problemId, int line) throws CoreException;
}
