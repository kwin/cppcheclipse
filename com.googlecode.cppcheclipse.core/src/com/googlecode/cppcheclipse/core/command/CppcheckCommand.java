package com.googlecode.cppcheclipse.core.command;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXException;

import com.googlecode.cppcheclipse.core.IConsole;
import com.googlecode.cppcheclipse.core.IPreferenceConstants;
import com.googlecode.cppcheclipse.core.Problem;

public class CppcheckCommand extends AbstractCppcheckCommand {
	private final static String ARGUMENTS = "--xml";

	private final Collection<String> arguments;
	private String advancedArguments;
	
	public CppcheckCommand(IConsole console, IPreferenceStore settingsStore, IPreferenceStore advancedSettingsStore, Collection<String> userIncludePaths, Collection<String> systemIncludePaths) {
		super(console);
		arguments = new LinkedList<String>();
		arguments.add(ARGUMENTS);
		
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
		
		// use advanced arguments
		advancedArguments = advancedSettingsStore.getString(IPreferenceConstants.P_ADVANCED_ARGUMENTS).trim();
		if (advancedArguments.length() == 0) {
			advancedArguments = null;
		}
	}
	
	public Collection<Problem> run(String filename, IFile file, IProgressMonitor monitor)
			throws XPathExpressionException, ParserConfigurationException,
			SAXException, IOException, InterruptedException, ProcessExecutionException {
		
		CppcheckProcess process = run(arguments.toArray(new String[0]), advancedArguments, new String[] { filename },  monitor);
		Collection<Problem> problems = parseXMLStream(process.getErrorStream(), file);
		process.close();
		return problems;
	}
}
