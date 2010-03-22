package com.googlecode.cppcheclipse.ui.commands;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.ui.Builder;
import com.googlecode.cppcheclipse.ui.Messages;

public class RunCodeAnalysis extends AbstractResourceSelectionJobCommand {
	
		private static class RunCodeAnalysisJob extends ResourceSelectionJob {

			private final Builder builder;
			public RunCodeAnalysisJob(String name) {
				super(name);
				builder = new Builder();
			}

			@Override
			protected void runResource(IResource resource,
					IProgressMonitor monitor) {
				SubProgressMonitor subMon = new SubProgressMonitor(monitor,
						100);
				try {
					builder.processResource(resource, subMon);
				} catch (CoreException e1) {
					CppcheclipsePlugin.showError(Messages.bind(
							Messages.RunCodeAnalysis_Error, resource.getName()),
							e1);
				}
				subMon.done();
			}
			
		}
	
	
		@Override
		ResourceSelectionJob getJob() {
			return new RunCodeAnalysisJob(Messages.RunCodeAnalysis_JobName);
		}
}
