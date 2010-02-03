package com.googlecode.cppcheclipse.ui.actions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.ui.Messages;

public class RunCodeAnalysis extends AbstractHandler {
	private static final QualifiedName SELECTION_PROPERTY = new QualifiedName("com.googlecode.cppcheclipse", "JobSelection");

	private IStructuredSelection getEditorSelection(IEditorPart editor) {
		if (editor == null) {
			return null;
		}
		IEditorInput input = editor.getEditorInput();
		if (input == null) {
			return null;
		}
		
		IFileEditorInput fileInput = (IFileEditorInput) input.getAdapter(IFileEditorInput.class);
		if (fileInput == null) {
			return null;
		}
		List<IFile> fileList = new LinkedList<IFile>();
		fileList.add(fileInput.getFile());
		return new StructuredSelection(fileList);
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get selection
		ISelection selection = HandlerUtil.getCurrentSelection(event); 
		if (!(selection instanceof IStructuredSelection)) {
			if (selection instanceof ITextSelection) {
				selection = getEditorSelection(HandlerUtil.getActiveEditor(event));
				if (selection == null) {
					return null;
				}
			}
		}
		
		Job job = new Job(Messages.RunCodeAnalysis_JobName) {
			@SuppressWarnings("unchecked")
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				IStructuredSelection ss = (IStructuredSelection)getProperty(SELECTION_PROPERTY);
			
				int count = ss.size() * 100;
				monitor.beginTask(getName(), count);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				Builder builder = new Builder();
				for (Iterator iterator = ss.iterator(); iterator.hasNext();) {
					Object elem = iterator.next();
					// Adapt the first element to a file.
					 if (!(elem instanceof IAdaptable))
					      continue;
					   IResource res = (IResource) ((IAdaptable) elem).getAdapter(IResource.class);
					   if (res == null)
					      continue;
					   
					SubProgressMonitor subMon = new SubProgressMonitor(
								monitor, 100);
						try {
							builder.processResource(res, subMon);
						} catch (CoreException e1) {
							CppcheclipsePlugin.showError(Messages.bind(Messages.RunCodeAnalysis_Error, res.getName()), e1);
						}
						subMon.done();
					
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setProperty(SELECTION_PROPERTY, (IStructuredSelection)selection);
		job.setUser(true);
		job.schedule();

		return null;
	}
}

