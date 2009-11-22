package com.googlecode.cppcheclipse.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;


public interface IProblemReporter {

	public static final String ID_ATTRIBUTE = "problemId";
	
	public abstract void reportProblem(Problem problem) throws CoreException;

	public abstract void deleteMarkers(IResource file) throws CoreException;

	public abstract void deleteAllMarkers() throws CoreException;

}