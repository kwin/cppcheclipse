package com.googlecode.cppcheclipse.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;


public interface IProblemReporter {

	
	
	/**
	 * Reports problem in problem view.
	 * @param problem the problem to report
	 * @throws CoreException
	 */
	public abstract void reportProblem(Problem problem) throws CoreException;

	/**
	 * Deletes all cppcheck markers in the given resource.
	 * @param resource
	 * @throws CoreException
	 */
	public abstract void deleteMarkers(IResource resource, boolean isRecursive) throws CoreException;

	/**
	 * Deletes all cppcheck markers from the whole workspace
	 * @throws CoreException
	 */
	public abstract void deleteAllMarkers() throws CoreException;

}