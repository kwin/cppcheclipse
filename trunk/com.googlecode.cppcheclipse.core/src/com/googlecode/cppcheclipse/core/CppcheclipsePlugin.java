package com.googlecode.cppcheclipse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CppcheclipsePlugin extends AbstractUIPlugin {

	// The shared instance
	private static CppcheclipsePlugin plugin;
	
	private IPersistentPreferenceStore workspacePreferenceStore, configurationPreferenceStore;

	private ProblemProfile profile;
	
	/**
	 * The constructor
	 */
	public CppcheclipsePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// don't create new threads in here, otherwise class-loading can be delayed by 5 seconds (if the class is not local)
		// see EclipseLazyStarter.postFindLocalClass(String, Class, ClasspathManager) line: 111	
		profile = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		profile = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CppcheclipsePlugin getDefault() {
		return plugin;
	}

	public static String getId() {
		return getDefault().getBundle().getSymbolicName();
	}

	public static IPersistentPreferenceStore getProjectPreferenceStore(IProject project, boolean useExtendedSearchContext) {
		// Create an overlay preference store and fill it with properties
		ProjectScope ps = new ProjectScope(project);
		ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, getId());
		if (useExtendedSearchContext) {
			scoped.setSearchContexts(new IScopeContext[] { ps,
				new InstanceScope() });
		}
		PreferenceInitializer.initializePropertiesDefault(scoped);
		return scoped;
	}

	public static IPersistentPreferenceStore getWorkspacePreferenceStore() {
		return getDefault().getInternalWorkspacePreferenceStore();
	}
	
	private IPersistentPreferenceStore getInternalWorkspacePreferenceStore() {
		if (workspacePreferenceStore == null) {
			workspacePreferenceStore = new ScopedPreferenceStore(new InstanceScope(), getId());
		}
		return workspacePreferenceStore;
	}
	
	public static IPersistentPreferenceStore getConfigurationPreferenceStore() {
		return getDefault().getInternalConfigurationPreferenceStore();
	}
	
	private IPersistentPreferenceStore getInternalConfigurationPreferenceStore() {
		if (configurationPreferenceStore == null) {
			configurationPreferenceStore = new ScopedPreferenceStore(new ConfigurationScope(), getId());
		}
		return configurationPreferenceStore;
	}
	
	public static ProblemProfile getNewProblemProfile(IConsole console, IPreferenceStore store) throws CloneNotSupportedException {
		return getDefault().getInternalNewProblemProfile(console, store);
	}
	
	synchronized private ProblemProfile getInternalNewProblemProfile(IConsole console, IPreferenceStore store) throws CloneNotSupportedException {
		if (profile == null) {
			profile = new ProblemProfile(console, getConfigurationPreferenceStore(), null);
		}
		// use old problem profile
		ProblemProfile newProfile = (ProblemProfile) profile.clone();
		newProfile.loadFromPreferences(store);
		return newProfile;
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(int severity, int style, String message,
			Throwable exception) {

		IStatus status = new Status(severity, getId(), 1, message, exception);
		StatusManager.getManager().handle(status, style);
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(IStatus.ERROR, StatusManager.LOG, "Internal Error", e); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void log(String message) {
		log(IStatus.ERROR, StatusManager.LOG, message, null);
	}

	public static void showError(String message, Throwable e) {
		log(IStatus.ERROR, StatusManager.SHOW|StatusManager.LOG, message, e);
	}
}
