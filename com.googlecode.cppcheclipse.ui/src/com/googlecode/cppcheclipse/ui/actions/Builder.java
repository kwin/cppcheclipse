package com.googlecode.cppcheclipse.ui.actions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.core.runtime.SubProgressMonitor;

import com.googlecode.cppcheclipse.core.Checker;
import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.IProblemReporter;
import com.googlecode.cppcheclipse.ui.Console;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.UpdateCheck;
import com.googlecode.cppcheclipse.ui.marker.ProblemReporter;

public class Builder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "com.googlecode.cppcheclipse.Builder"; //$NON-NLS-1$

	private static final String[] VALID_EXTENSIONS = { ".cpp", ".cxx", ".c++", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			".cc", ".c", ".txx" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private Checker checker;
	private IProject project;
	private final IConsole console;
	private final IProblemReporter problemReporter;

	public Builder() {
		super();
		checker = null;
		project = null;
		console = new Console();
		problemReporter = new ProblemReporter();
		
		if (UpdateCheck.needUpdateCheck()) {
			new UpdateCheck(true).check();
		}
	}

	public class DeltaVisitor implements IResourceDeltaVisitor {

		private final IProgressMonitor monitor;

		public DeltaVisitor(IProgressMonitor monitor, IResourceDelta delta) {
			this.monitor = monitor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				processResource(resource, new SubProgressMonitor(monitor, 1));
				break;
			case IResourceDelta.REMOVED:
				if (resource instanceof IFile) {
					problemReporter.deleteMarkers((IFile)resource);
				}
				monitor.worked(1);
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				processResource(resource, new SubProgressMonitor(monitor, 1));
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	private class ResourceVisitor implements IResourceVisitor {
		private final IProgressMonitor monitor;

		public ResourceVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				// create translation unit and access index
				String fileName = file.getLocation().makeAbsolute()
						.toOSString();
				if (shouldCheck(fileName)) {
					processFile(file, fileName);
				}
			}
			// return true to continue visiting children.
			return !monitor.isCanceled();
		}

		protected void processFile(IFile file, String fileName)
				throws CoreException {
			monitor.setTaskName(Messages.bind(Messages.Builder_TaskName, file
					.getName()));

			// (re-)initialize checker if necessary (first use or different
			// project)
			try {
				IProject currentProject = file.getProject();
				if (checker == null || !project.equals(currentProject)) {
					Collection<String> includePaths = getIncludePaths(currentProject);
					checker = new Checker(console, CppcheclipsePlugin
							.getProjectPreferenceStore(currentProject, true),
							CppcheclipsePlugin.getWorkspacePreferenceStore(),
							currentProject, includePaths, new ProblemReporter());
					project = currentProject;
				}
			} catch (Exception e) {
				CppcheclipsePlugin
						.showError("Could not initialize cppcheck", e); //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR, CppcheclipsePlugin
						.getId(), "Could not initialize cppcheck", e); //$NON-NLS-1$
				throw new CoreException(status);
			}
			try {
				checker.processFile(fileName, file, monitor);
			} catch (InterruptedException e) {
				CppcheclipsePlugin.log(e);
			} catch (Exception e) {
				CppcheclipsePlugin.showError("Error checking resource " //$NON-NLS-1$
						+ file.getName(), e);
			}
			monitor.worked(1);
		}

		/**
		 * @see http://cdt-devel-faq.wikidot.com/#toc21
		 * @return
		 */
		private Collection<String> getIncludePaths(IProject project) {
			Collection<String> paths = new LinkedList<String>();
			String workspacePath = project.getWorkspace().getRoot()
					.getRawLocation().makeAbsolute().toOSString();

			ICProjectDescription projectDescription = CoreModel.getDefault()
					.getProjectDescription(project);
			if (projectDescription == null) {
				return paths;
			}
			ICConfigurationDescription activeConfiguration = projectDescription
					.getActiveConfiguration(); // or another config
			if (activeConfiguration == null) {
				return paths;
			}
			ICFolderDescription folderDescription = activeConfiguration
					.getRootFolderDescription(); // or use
			// getResourceDescription(IResource),
			// or pick one from
			// getFolderDescriptions()
			ICLanguageSetting[] languageSettings = folderDescription
					.getLanguageSettings();

			// fetch the include settings from the first tool which supports c
			for (ICLanguageSetting languageSetting : languageSettings) {
				String extensions[] = languageSetting.getSourceExtensions();
				for (String extension : extensions) {
					if ("cpp".equalsIgnoreCase(extension)) { //$NON-NLS-1$
						ICLanguageSettingEntry[] includePathSettings = languageSetting
								.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
						for (ICLanguageSettingEntry includePathSetting : includePathSettings) {
							String path = includePathSetting.getValue();
							if ((includePathSetting.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == ICSettingEntry.VALUE_WORKSPACE_PATH) {
								path = workspacePath + path;
							}
							paths.add(path);
						}
					}
				}
				if (paths.size() > 0) {
					return paths;
				}
			}
			return paths;
		}
	}
	
	private class ResourceVisitorCounter extends ResourceVisitor {

		private int count;
		
		public ResourceVisitorCounter() {
			super(new NullProgressMonitor());
			count = 0;
		}

		@Override
		protected void processFile(IFile file, String fileName)
				throws CoreException {
			count++;
		}
		
		public int getCount() {
			return count;
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
		new ProblemReporter().deleteAllMarkers();
		super.clean(monitor);
	}

	public void processResource(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		if (resource.getProject() == null)
			return;
		
		// first count all relevant resources including and below resource
		ResourceVisitorCounter visitorCounter = new ResourceVisitorCounter();
		resource.accept(visitorCounter);
		
		// setup monitor
		monitor.beginTask("Checking " + resource.getName(), visitorCounter.getCount());
		resource.accept(new ResourceVisitor(monitor));
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		processResource(getProject(), monitor);
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// TODO: improve progress monitor
		monitor.beginTask("Checking changed resources", delta.getAffectedChildren().length);
		delta.accept(new DeltaVisitor(monitor, delta));
	}

	public static boolean shouldCheck(String filename) {
		// check for valid extension
		for (String validExtension : VALID_EXTENSIONS) {
			if (filename.toLowerCase().endsWith(validExtension))
				return true;
		}
		return false;
	}
}
