package com.googlecode.cppcheclipse.ui.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.Utils;

public class ReportBugResolution implements IMarkerResolution {

	private static final String REPORT_URL = "http://trac.cppcheck.net/report"; //$NON-NLS-1$
	public String getLabel() {
		return Messages.ReportBug_Label;
	}

	public void run(IMarker marker) {
		try {
			Utils.openUrl(REPORT_URL);
		} catch (Exception e) {
			CppcheclipsePlugin.logError("Could not open bug report page", e);
		}

	}

}
