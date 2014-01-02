package com.googlecode.cppcheclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;

import com.googlecode.cppcheclipse.core.utils.SerializeHelper;

public class Suppression {
	private final File file;
	private final String problemId;
	private final int line;
	
	private final static String PROBLEM_ID_ALL = "allProblems";
	private final static int LINE_ALL = Integer.MAX_VALUE;
	
	private final static String DELIMITER = ";";
	public Suppression(File file, IProject project) {
		this(file, PROBLEM_ID_ALL);
	}
	
	public Suppression(File file, String problemId) {
		this(file, problemId, LINE_ALL);
	}
	
	public Suppression(File file, String problemId, int line) {
		super();
		this.file = file;
		this.problemId = problemId;
		this.line = line;
	}
	
	/**
	 * 
	 * @return the absolute file of this suppression
	 */
	public File getFile(IProject project) {
		return getFile(true, project);
	}
	
	/**
	 * @param absolute if true, returns an absolute path, otherwise it might be relative
	 * @return the file of this suppression
	 */
	public File getFile(boolean absolute, IProject project) {
		final File file;
		if (absolute && !this.file.isAbsolute()) {
			file = new File(project.getLocation().toFile(), this.file.toString());
		} else {
			file = this.file;
		}
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
	
	public boolean isSuppression(File absoluteFile, IProject project) {
		return isSuppression(absoluteFile, PROBLEM_ID_ALL, LINE_ALL, project);
	}
	
	public boolean isAllLines() {
		return line == LINE_ALL;
	}
	
	/**
	 * 
	 * @param file (must be absolute)
	 * @param problemId
	 * @param line
	 * @return true if the given problem should be suppressed
	 */
	public boolean isSuppression(File file, String problemId, int line, IProject project) {
		if (!file.equals(getFile(project)) && !file.toString().startsWith(getFile(project).toString() + File.separator))
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

	public String serialize() throws IOException {
		StringBuffer serialization = new StringBuffer();
		// serialize in a platform-portable way
		String path = SerializeHelper.toString(file);
		serialization.append(path).append(DELIMITER);
		serialization.append(problemId).append(DELIMITER);
		serialization.append(line);
		return serialization.toString();
	}
	
	public static Suppression deserialize(String serialization) throws IOException, ClassNotFoundException {
		StringTokenizer tokenizer = new StringTokenizer(serialization, DELIMITER);
		File file =  (File) SerializeHelper.fromString(tokenizer.nextToken());
		String problemId = tokenizer.nextToken();
		int line = Integer.parseInt(tokenizer.nextToken());
		return new Suppression(file, problemId, line);
	}
}