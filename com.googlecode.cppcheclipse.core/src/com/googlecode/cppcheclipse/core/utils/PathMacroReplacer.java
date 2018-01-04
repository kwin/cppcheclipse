package com.googlecode.cppcheclipse.core.utils;

public class PathMacroReplacer {
	
	// TODO implement the VariablesPlugin
	
	public static String process(String input) {
		return input.replace("${eclipse_home}", System.getProperty("eclipse.home.location").replace("file:/", ""));
	}

}
