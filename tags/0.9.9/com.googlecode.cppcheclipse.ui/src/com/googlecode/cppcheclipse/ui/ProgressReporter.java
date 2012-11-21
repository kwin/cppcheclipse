package com.googlecode.cppcheclipse.ui;

import org.eclipse.core.runtime.IProgressMonitor;

import com.googlecode.cppcheclipse.core.IProgressReporter;

public class ProgressReporter implements IProgressReporter {

	private final IProgressMonitor monitor;
	private int reportedProgress;

	public ProgressReporter(IProgressMonitor monitor) {
		super();
		this.monitor = monitor;
		reportedProgress = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.googlecode.cppcheclipse.core.IProgressReporter#reportProgress(java
	 * .lang.String, java.lang.Integer)
	 */
	public void reportProgress(String currentlyCheckedFilename,
			Integer numFilesChecked) {
		if (currentlyCheckedFilename != null) {
			monitor.subTask(Messages.bind(Messages.ProgressReporter_TaskName,
					currentlyCheckedFilename));
		}
		if (numFilesChecked != null) {
			int workUnits = numFilesChecked - reportedProgress;
			if (workUnits > 0) {
				monitor.worked(workUnits);
				reportedProgress = numFilesChecked;
			}
		}
	}

}
