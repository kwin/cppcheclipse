package com.googlecode.cppcheclipse.ui.commands;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.marker.ProblemReporter;

public class ClearMarkers extends AbstractResourceSelectionJobCommand {

	
	private static class ClearMarkersJob extends ResourceSelectionJob {

		private final ProblemReporter reporter;
		public ClearMarkersJob(String name) {
			super(name);
			reporter = new ProblemReporter();
		}

		@Override
		protected void runResource(IResource resource,
				IProgressMonitor monitor) {
			monitor.beginTask("", 1);
			try {
				reporter.deleteMarkers(resource, true);
			} catch (CoreException e) {
				CppcheclipsePlugin.showError(Messages.bind(
						Messages.ClearMarkers_Error, resource.getName()),
						e);
			}
			monitor.worked(1);
		}
		
	}


	@Override
	ResourceSelectionJob getJob() {
		return new ClearMarkersJob(Messages.ClearMarkers_JobName);
	}
	

}
