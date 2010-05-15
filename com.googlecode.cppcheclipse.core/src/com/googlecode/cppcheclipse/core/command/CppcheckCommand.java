package com.googlecode.cppcheclipse.core.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.Appendages;
import com.googlecode.cppcheclipse.core.Checker;
import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.core.IProgressReporter;
import com.googlecode.cppcheclipse.core.Problem;

public class CppcheckCommand extends AbstractCppcheckCommand {
	
	private final static String DELIMITER = ";";
	private final static String ERROR_FORMAT = "{file}"+DELIMITER+"{line}"+DELIMITER+"{severity}"+DELIMITER+"{id}"+DELIMITER+"{message}";
	private final static String[] DEFAULT_ARGUMENTS = {"--template", ERROR_FORMAT};
	
	/**
	 * pattern recognizes
	 * 2/2 files checked 100% done
	 */
	private final static Pattern PROGRESS_PATTERN = Pattern.compile("^((\\d)*)/(\\d)* files checked (\\d)*% done");
	
	/**
	 * pattern recognizes
	 * Checking src/test.1.cpp...
	 */
	private final static Pattern FILE_PATTERN = Pattern.compile("^Checking (.*)...");

	private final Collection<String> arguments;
	private String advancedArguments;
	
	
	public CppcheckCommand(IConsole console, IPreferenceStore settingsStore, IPreferenceStore advancedSettingsStore, Collection<String> userIncludePaths, Collection<String> systemIncludePaths) {
		super(console, DEFAULT_ARGUMENTS);
		arguments = new LinkedList<String>();
		
		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_ALL)) {
			arguments.add("--enable=all");
		} else {
		
			List<String> enableFlags = new LinkedList<String>();
			if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_STYLE)) {
				enableFlags.add("style");
			}
			if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_EXCEPT_NEW)) {
				enableFlags.add("exceptNew");
			}
			if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_EXCEPT_REALLOC)) {
				enableFlags.add("exceptRealloc");
			}
			if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_POSSIBLE_ERROR)) {
				enableFlags.add("possibleError");
			}
			// when unused-function check is on, -j is not available!
			boolean checkUnusedFunctions = settingsStore.getBoolean(IPreferenceConstants.P_CHECK_UNUSED_FUNCTIONS);
			if (checkUnusedFunctions) {
				enableFlags.add("unusedFunctions");
			} else {
				arguments.add("-j");
				arguments.add(String.valueOf(settingsStore.getInt(IPreferenceConstants.P_NUMBER_OF_THREADS)));
			}
			
			if (!enableFlags.isEmpty()) {
				arguments.add("--enable=" + StringUtils.join(enableFlags, ","));
			}
		}
		
		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_VERBOSE)) {
			arguments.add("--verbose");
		}
		
		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_FORCE)) {
			arguments.add("--force");
		}
		
		if (settingsStore.getBoolean(IPreferenceConstants.P_CHECK_DEBUG)) {
			arguments.add("--debug");
		}
		
		if (settingsStore.getBoolean(IPreferenceConstants.P_USE_INLINE_SUPPRESSIONS)) {
			arguments.add("--inline-suppr");
		}
		
		// TODO: enable when bug 878 of cppcheck is solved, see http://sourceforge.net/apps/trac/cppcheck/ticket/878
		/*
		if (settingsStore.getBoolean(IPreferenceConstants.P_FOLLOW_SYSTEM_INCLUDES)) {
			for (String path: systemIncludePaths) {
				arguments.add("-I");
				arguments.add(path);
			}
		}*/
		
		if (settingsStore.getBoolean(IPreferenceConstants.P_FOLLOW_USER_INCLUDES)) {
			for (String path: userIncludePaths) {
				arguments.add("-I");
				arguments.add(path);
			}
		}
		
		Appendages appendages = new Appendages(advancedSettingsStore);
		for (File appendFile : appendages) {
			arguments.add("--append="+appendFile.toString());
		}
		
		// use advanced arguments
		advancedArguments = advancedSettingsStore.getString(IPreferenceConstants.P_ADVANCED_ARGUMENTS).trim();
		if (advancedArguments.length() == 0) {
			advancedArguments = null;
		}
		 
		
	}
	
	public void run(Checker checker, IProgressReporter progressReporter, IProject project, List<IFile> files, IProgressMonitor monitor)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException, InterruptedException, ProcessExecutionException, CoreException {
		
		// convert list of files to filenames
		List<String> filenames = new LinkedList<String>();
		for (IFile file : files) {
			filenames.add(file.getProjectRelativePath().toOSString());
		}
		
		setWorkingDirectory(project.getLocation().toFile());
		CppcheckProcessResultHandler resultHandler = runInternal(arguments.toArray(new String[0]), advancedArguments, filenames.toArray(new String[0]));
		
		List<Problem> problems = new LinkedList<Problem>();
		try {
			while (resultHandler.isRunning()) {
				Thread.sleep(SLEEP_TIME_MS);
				if (monitor.isCanceled()) {
					watchdog.destroyProcess();
					throw new InterruptedException("Process manually killed");
				}
				
				// parse output
				parseResultLines(project, getErrorReader(), problems);
				
				if (!problems.isEmpty()) {
					// give out problems
					checker.reportProblems(problems);
					problems.clear();
				}
				
				// parse progress
				parseProgressLines(getOutputReader(), progressReporter);
				
				// don't use parsed lines twice
			}
			waitForExit(resultHandler, monitor);
		} finally {
			if (resultHandler.isRunning()) {
				// always destroy process if it is still running here
				watchdog.destroyProcess();
			}
		}
	}
	
	public static void parseResultLines(IProject project, BufferedReader reader, List<Problem> problems) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			Problem problem = parseResult(line, project);
			if (problem != null) {
				problems.add(problem);
			}
		}
	}
	
	public static Problem parseResult(String line, IProject project) {
		StringTokenizer tokenizer = new StringTokenizer(line, DELIMITER);
		
		/**
		 * line should have the following format
		 * <file>;<line>;<severity>;<id>;<message>
		 */
		try {
			File filename = new File(tokenizer.nextToken());
			int lineNumber = Integer.parseInt(tokenizer.nextToken());
			String severity = tokenizer.nextToken();
			String id = tokenizer.nextToken();
			
			String message = tokenizer.nextToken();
			// messages consists of all following tokens
			while (tokenizer.hasMoreTokens()) {
				message += DELIMITER + tokenizer.nextToken();
			}
			return new Problem(id, message, severity, filename, project,
					lineNumber);
		
		} catch(NoSuchElementException e1) {
			// ignore line
		} catch (NumberFormatException e2) {
			// ignore line
		}
		return null;
	}
	
	public static void parseProgressLines(BufferedReader reader, IProgressReporter progressReporter) throws IOException {
		String line;
		String fileName = null;
		Integer fileNumber = null;
		
		while ((line = reader.readLine()) != null) {
			fileName = parseFilename(line);
			fileNumber = parseProgress(line);
			progressReporter.reportProgress(fileName, fileNumber);
		}
	}
	
	public static String parseFilename(String line) {
		Matcher fileMatcher = FILE_PATTERN.matcher(line);
		if (fileMatcher.matches()) {
			return fileMatcher.group(1);
		}
		return null;
	}
	
	public static Integer parseProgress(String line) {
		Matcher progressMatcher = PROGRESS_PATTERN.matcher(line);
		if (progressMatcher.matches()) {
			String fileNumber = progressMatcher.group(1);
			if (fileNumber != null) {
				return new Integer(fileNumber);
			}
		}
		return null;
	}
}
