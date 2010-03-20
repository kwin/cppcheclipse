package com.googlecode.cppcheclipse.ui.commands;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;

public abstract class ResourceSelectionJob extends Job {

	public ResourceSelectionJob(String name) {
		super(name);
	}

	static IResource getIResource(Object element) {
		// Adapt the first element to a file.
		if (!(element instanceof IAdaptable))
			return null;
		IResource res = (IResource) ((IAdaptable) element)
				.getAdapter(IResource.class);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStructuredSelection selection = (IStructuredSelection) getProperty(AbstractResourceSelectionJobCommand.SELECTION_PROPERTY);
		
		int count = selection.size() * 100;
		monitor.beginTask(getName(), count);
		if (monitor.isCanceled())
			return Status.CANCEL_STATUS;
		
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			IResource res = getIResource(iterator.next());
			if (res == null)
				continue;
			
			runResource(res, monitor);
			
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
		}
		return Status.OK_STATUS;
	}

	abstract protected void runResource(IResource resource, IProgressMonitor monitor);
}
