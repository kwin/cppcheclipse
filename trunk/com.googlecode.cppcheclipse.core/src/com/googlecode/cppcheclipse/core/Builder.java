package com.googlecode.cppcheclipse.core;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

public class Builder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "com.googlecode.cppcheclipse.Builder";

	private Checker checker;

	public Builder() {
		super();
		checker = null;
	}

	public class DeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.cdt.codan.internal.core.ICodanBuilder#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				processResource(resource, new NullProgressMonitor());
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				processResource(resource, new NullProgressMonitor());
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	public class ResourceVisitor implements IResourceVisitor {
		private final Checker checker;
		private final IProgressMonitor monitor;
		
		public ResourceVisitor(Checker checker,  IProgressMonitor monitor) {
			this.checker = checker;
			this.monitor = monitor;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			try {
				checker.processResource(resource, monitor);
			} catch (Exception e) {
				CppcheclipsePlugin.showError("Error checking resource " + resource.getName(), e);
			}
			// return true to continue visiting children.
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse
	 * .core.runtime.IProgressMonitor)
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		ProblemReporter.deleteAllMarkers();
		super.clean(monitor);
	}

	public void processResource(IResource resource, IProgressMonitor monitor) throws CoreException {
		if (resource.getProject() == null)
			return;
		try {
			if (checker == null || !checker.isValidProject(resource)) {
				checker = new Checker(resource.getProject());
			}
		} catch (Exception e) {
			CppcheclipsePlugin.showError("Could not initialize cppcheck", e);
			IStatus status = new Status(IStatus.ERROR, CppcheclipsePlugin.getId(), "Could not initialize cppcheck", e);
			throw new CoreException(status);
		}
		resource.accept(getResourceVisitor(checker, monitor));
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		processResource(getProject(), monitor);
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new DeltaVisitor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.ICodanBuilder#getResourceVisitor()
	 */
	public ResourceVisitor getResourceVisitor(Checker checker, IProgressMonitor monitor) {
		return new ResourceVisitor(checker, monitor);
	}
}
