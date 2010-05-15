package com.googlecode.cppcheclipse.ui.marker;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.googlecode.cppcheclipse.core.SuppressionProfile;
import com.googlecode.cppcheclipse.ui.Messages;

public class SuppressProblemInLineResolution extends SuppressResolution {

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.marker.SuppressResolution#getLabel()
	 */
	@Override
	public String getLabel() {
		return Messages.SuppressProblemInLineResolution_Label;
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.ui.marker.SuppressResolution#suppress(com.googlecode.cppcheclipse.core.SuppressionProfile, org.eclipse.core.resources.IFile, java.lang.String, int)
	 */
	@Override
	protected void suppress(SuppressionProfile profile, IResource resource, File file,
			String problemId, int line) throws CoreException {
		profile.addProblemInLineSuppression(file, problemId, line);
		
	}

}
