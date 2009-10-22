package com.googlecode.cppcheclipse.core;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

public class Builder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "com.googlecode.cppcheclipse.Builder";

	private static final String[] VALID_EXTENSIONS = { ".cpp", ".cxx", ".c++",
			".cc", ".c" };

	private Checker checker;
	private IProject project;

	public Builder() {
		super();
		checker = null;
		project = null;
	}

	public class DeltaVisitor implements IResourceDeltaVisitor {

		private final IProgressMonitor monitor;

		public DeltaVisitor(IProgressMonitor monitor, IResourceDelta delta) {
			// IResourceDelta[] deltas = delta.getAffectedChildren();
			this.monitor = monitor;
			// monitor.beginTask("Running cppcheck", deltas.length);
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
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				processResource(resource, new SubProgressMonitor(monitor, 1));
				break;
			}
			monitor.worked(1);
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
				processFile(file);
			}

			// return true to continue visiting children.
			return !monitor.isCanceled();
		}

		private void processFile(IFile file) throws CoreException {
			// create translation unit and access index
			String fileName = file.getLocation().makeAbsolute().toOSString();
			if (shouldCheck(fileName)) {
				monitor.setTaskName("Checking " + file.getName());
				
				// (re-)initialize checker if necessary (first use or different project)
				try {
					IProject currentProject = file.getProject();
					if (checker == null || !project.equals(currentProject)) {
						Collection<String> includePaths = getIncludePaths(currentProject);
						checker = new Checker(CppcheclipsePlugin.getProjectPreferenceStore(currentProject, true), CppcheclipsePlugin.getWorkspacePreferenceStore(), includePaths);
						project = currentProject;
					}
				} catch (Exception e) {
					CppcheclipsePlugin.showError(
							"Could not initialize cppcheck", e);
					IStatus status = new Status(IStatus.ERROR,
							CppcheclipsePlugin.getId(),
							"Could not initialize cppcheck", e);
					throw new CoreException(status);
				}
				try {
					checker.processFile(fileName, file, monitor);
				} catch (InterruptedException e) {
					CppcheclipsePlugin.log(e);
				} catch (Exception e) {
					CppcheclipsePlugin.showError("Error checking resource "
							+ file.getName(), e);
				}
				monitor.worked(1);
			}
		}

		private boolean shouldCheck(String filename) {
			// check for valid extension
			for (String validExtension : VALID_EXTENSIONS) {
				if (filename.endsWith(validExtension))
					return true;
			}
			return false;
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
					if ("cpp".equalsIgnoreCase(extension)) {
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

	public void processResource(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		if (resource.getProject() == null)
			return;

		resource.accept(new ResourceVisitor(monitor));
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		processResource(getProject(), monitor);
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new DeltaVisitor(monitor, delta));
	}
}
