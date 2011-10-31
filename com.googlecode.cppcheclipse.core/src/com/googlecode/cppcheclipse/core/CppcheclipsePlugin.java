package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.command.ProcessExecutionException;
import com.googlecode.cppcheclipse.core.utils.IHttpClientService;

/**
 * The activator class controls the plug-in life cycle
 */
public class CppcheclipsePlugin extends AbstractUIPlugin implements IPropertyChangeListener {

	// The shared instance
	private static CppcheclipsePlugin plugin;
	
	private IPersistentPreferenceStore workspacePreferenceStore, configurationPreferenceStore;

	private ProblemProfile profile;
	
	private ServiceTracker tracker;
	
	private Collection<IPropertyChangeListener> binaryPathChangeListeners;
	
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
	public synchronized void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// don't create new threads in here, otherwise class-loading can be delayed by 5 seconds (if the class is not local)
		// see EclipseLazyStarter.postFindLocalClass(String, Class, ClasspathManager) line: 111	
		profile = null;
		
		binaryPathChangeListeners = new CopyOnWriteArrayList<IPropertyChangeListener>();
		
		tracker = new ServiceTracker(context, IHttpClientService.class.getName(), null);
		tracker.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public synchronized void stop(BundleContext context) throws Exception {
		tracker.close();
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

	public static IPersistentPreferenceStore getProjectPreferenceStore(IProject project) {
		// Create an overlay preference store and fill it with properties
		ProjectScope ps = new ProjectScope(project);
		ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps, getId());
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
	
	public static ProblemProfile getNewProblemProfile(IConsole console, IPreferenceStore store) throws CloneNotSupportedException, XPathExpressionException, IOException, InterruptedException, ParserConfigurationException, SAXException, ProcessExecutionException {
		return getDefault().getInternalNewProblemProfile(console, store);
	}
	
	// only call this once for the main problem profile
	private void registerChangeListener() {
		// register change listener for binary path
		getConfigurationPreferenceStore()
				.addPropertyChangeListener(this);
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (IPreferenceConstants.P_BINARY_PATH.equals(event
				.getProperty())) {
			for (IPropertyChangeListener binaryPathChangeListener : binaryPathChangeListeners) {
				binaryPathChangeListener.propertyChange(event);
			}
		}
	}
	
	public void addChangeListener(IPropertyChangeListener listener) {
		binaryPathChangeListeners.add(listener);
	}
	
	public boolean removeChangeListener(IPropertyChangeListener listener) {
		return binaryPathChangeListeners.remove(listener);
	}
	
	private synchronized ProblemProfile getInternalNewProblemProfile(IConsole console, IPreferenceStore store) throws CloneNotSupportedException, XPathExpressionException, IOException, InterruptedException, ParserConfigurationException, SAXException, ProcessExecutionException {
		if (profile == null) {
			String binaryPath = CppcheclipsePlugin.getConfigurationPreferenceStore()
			.getString(IPreferenceConstants.P_BINARY_PATH);
			profile = new ProblemProfile(console, binaryPath);
			registerChangeListener();
			addChangeListener(profile);
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

	public static void logError(String message, Throwable e) {
		log(IStatus.ERROR, StatusManager.LOG, message, e); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void logError(String message) {
		logError(message, null);
	}
	
	public static void logInfo(String message, Throwable e) {
		log(IStatus.INFO, StatusManager.LOG, message, e);
	}
	
	public static void logInfo(String message) {
		logInfo(message, null);
	}
	
	public static void logWarning(String message, Throwable e) {
		log(IStatus.WARNING, StatusManager.LOG, message, e);
	} 
	
	public static void logWarning(String message) {
		logWarning(message, null);
	}

	public static void showError(String message, Throwable e) {
		log(IStatus.ERROR, StatusManager.SHOW|StatusManager.LOG, message, e);
	}
	
	public static IHttpClientService getHttpClientService() {
		return (IHttpClientService) getDefault().tracker.getService();
	}
}
