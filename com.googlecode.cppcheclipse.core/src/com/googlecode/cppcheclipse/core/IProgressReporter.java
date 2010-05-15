package com.googlecode.cppcheclipse.core;

public interface IProgressReporter {
	/**
	 * Reports progress in checking file
	 * @param currentlyCheckedFilename filename of the currently checked file (may be null to indicate nothing has changed since last report)
	 * @param numFilesChecked number of files which are already checked (may be null to indicate nothing has changed since last report)
	 */
	void reportProgress(String currentlyCheckedFilename, Integer numFilesChecked);
}
