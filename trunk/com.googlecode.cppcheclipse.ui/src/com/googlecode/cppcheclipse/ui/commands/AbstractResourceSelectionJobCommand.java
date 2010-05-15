package com.googlecode.cppcheclipse.ui.commands;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractResourceSelectionJobCommand extends AbstractHandler {

	static final QualifiedName SELECTION_PROPERTY = new QualifiedName(
			"com.googlecode.cppcheclipse", "JobSelection"); //$NON-NLS-1$ //$NON-NLS-2$
	
	private IStructuredSelection getEditorFileSelection(IEditorPart editor) {
		if (editor == null) {
			return null;
		}
		IEditorInput input = editor.getEditorInput();
		if (input == null) {
			return null;
		}

		IFileEditorInput fileInput = (IFileEditorInput) input
				.getAdapter(IFileEditorInput.class);
		if (fileInput == null) {
			return null;
		}
		List<IFile> fileList = new LinkedList<IFile>();
		fileList.add(fileInput.getFile());
		return new StructuredSelection(fileList);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		IStructuredSelection structuredSelection;
		if (!(selection instanceof IStructuredSelection)) {
			structuredSelection = getEditorFileSelection(HandlerUtil
					.getActiveEditor(event));

		} else {
			// maybe this is a structured selection which does not adapt to an
			// IResorce
			structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			// in that case, try to get the file via the current editor
			if (ResourceSelectionJob.getIResource(firstElement) == null) {
				structuredSelection = getEditorFileSelection(HandlerUtil.getActiveEditor(event));
			}
		}
		
		if (structuredSelection == null) {
			return null;
		}
		
		ResourceSelectionJob job = getJob();
		job.setProperty(SELECTION_PROPERTY, structuredSelection);
		job.setUser(true);
		job.schedule();

		return null;
	}
	
	abstract ResourceSelectionJob getJob();
}
