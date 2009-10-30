package com.googlecode.cppcheclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.ui.Messages;

public class RunCodeAnalysis implements IObjectActionDelegate {
	private ISelection sel;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		Job job = new Job(Messages.RunCodeAnalysis_JobName) {
			@SuppressWarnings("unchecked")
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				IStructuredSelection ss = (IStructuredSelection) sel;
				int count = ss.size() * 100;
				monitor.beginTask(getName(), count);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				int work = 0;
				Builder builder = new Builder();
				for (Iterator iterator = ss.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof IResource) {
						IResource res = (IResource) o;
						SubProgressMonitor subMon = new SubProgressMonitor(
								monitor, 100);
						try {
							builder.processResource(res, subMon);
						} catch (CoreException e1) {
							CppcheclipsePlugin.log(e1);
						}
						if (subMon.isCanceled())
							return Status.CANCEL_STATUS;
					}
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					monitor.worked(++work);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.sel = selection;
	}
}

