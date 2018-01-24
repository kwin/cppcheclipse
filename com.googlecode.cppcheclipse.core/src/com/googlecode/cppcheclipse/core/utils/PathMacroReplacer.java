package com.googlecode.cppcheclipse.core.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;

/**
 * This util class is helping replace path containing macros into real path.
 * 
 * Following macros will resolve to:
 * ${eclipse_home} to a Eclipse's installation folder.
 * ${project_loc} to the folder of a active project. 
 * ${workspace_loc} to the current open workspace.
 * 
 * https://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Fconcepts%2Fcpathvars.htm
 * 
 * @author Anton Krug
 * 
 */

public class PathMacroReplacer {
	
	public static String performMacroSubstitution(String input) {
		String ret = input;
		
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			ret = manager.performStringSubstitution(ret, false);
		} catch (CoreException e) {
			// in case of a issue, keep the path as it is and log error
			CppcheclipsePlugin.logError("Path macro subsitution failed", e); //$NON-NLS-1$
		}		
		
		return ret;
	}

}
