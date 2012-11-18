package com.googlecode.cppcheclipse.ui.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.Utils;

public class CheckDescriptionResolution implements IMarkerResolution {

	private static final String CHECK_DESCRIPTION_URL = "http://sourceforge.net/apps/mediawiki/cppcheck/index.php?title=Main_Page#Checks"; //$NON-NLS-1$
	public String getLabel() {
		return Messages.CheckDescriptionResolution_Label;
	}

	public void run(IMarker marker) {
		try {
			Utils.openUrl(CHECK_DESCRIPTION_URL);
		} catch (Exception e) {
			CppcheclipsePlugin.logError("Could not open check description page", e);
		}

	}

}
