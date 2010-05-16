package com.googlecode.cppcheclipse.core;

import org.eclipse.core.resources.IMarker;

public enum ProblemSeverity {
	Info(IMarker.SEVERITY_INFO), Warning(IMarker.SEVERITY_WARNING), Error(
			IMarker.SEVERITY_ERROR);
	private int value;

	private ProblemSeverity(int value) {
		this.value = value;
	}

	public int intValue() {
		return value;
	}

	public static String[] stringValues() {
		ProblemSeverity[] values = values();
		String[] svalues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			ProblemSeverity sev = values[i];
			svalues[i] = sev.toString();
		}
		return svalues;
	}
	
	// creates default problem severity from cppcheck severity (here called category)
	public static ProblemSeverity fromCategory(String category) {
		ProblemSeverity severity = ProblemSeverity.Error;
		if ("error".equals(category)) {
			severity = ProblemSeverity.Error;
		} else if ("style".equals(category)) {
			severity = ProblemSeverity.Warning;
		} else {
			CppcheclipsePlugin.log("Unknown severity detected: " + category);
		}
		return severity;
	}

}
