package com.googlecode.cppcheclipse.core;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;

public class Suppression {
	private final IFile file;
	private final String problemId;
	private final int line;
	
	private final static String PROBLEM_ID_ALL = "allProblems";
	private final static int LINE_ALL = Integer.MAX_VALUE;
	
	private final static String DELIMITER = ";";
	public Suppression(IFile file) {
		this(file, PROBLEM_ID_ALL);
	}
	
	public Suppression(IFile file, String problemId) {
		this(file, problemId, LINE_ALL);
	}
	
	public Suppression(IFile file, String problemId, int line) {
		super();
		this.file = file;
		this.problemId = problemId;
		this.line = line;
	}
	
	public IFile getFile() {
		return file;
	}

	public String getProblemId() {
		return problemId;
	}
	
	public int getLine() {
		return line;
	}
	
	public boolean isFileSuppression() {
		return problemId.equals(PROBLEM_ID_ALL);
	}
	
	public boolean isAllLines() {
		return line == LINE_ALL;
	}
	
	/**
	 * 
	 * @param file
	 * @param problemId
	 * @param line
	 * @return true if the given problem should be suppressed
	 */
	public boolean isSuppression(IFile file, String problemId, int line) {
		if (!file.equals(getFile()))
			return false;
		
		if (isFileSuppression())
			return true;

		if (getProblemId().equals(problemId)) {
			if (getLine() == LINE_ALL)
				return true;
			
			// check line
			if (getLine() == line)
				return true;
		}
		return false;
	}

	public String serialize() {
		StringBuffer serialization = new StringBuffer();
		String path = file.getProjectRelativePath().toPortableString();
		serialization.append(path).append(DELIMITER);
		serialization.append(problemId).append(DELIMITER);
		serialization.append(line);
		return serialization.toString();
	}
	
	public static Suppression deserialize(String serialization, IProject project) {
		StringTokenizer tokenizer = new StringTokenizer(serialization, DELIMITER);
		String path = tokenizer.nextToken();
		IFile file = project.getFile(Path.fromPortableString(path));
		String problemId = tokenizer.nextToken();
		int line = Integer.parseInt(tokenizer.nextToken());
		return new Suppression(file, problemId, line);
	}
}