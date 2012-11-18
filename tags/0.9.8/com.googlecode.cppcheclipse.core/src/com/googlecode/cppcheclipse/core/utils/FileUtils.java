package com.googlecode.cppcheclipse.core.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class FileUtils {
	
	static public File relativizeFile(File parent, File file) throws URISyntaxException {
		// use Uri.relativize
		// regard the bugs in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6226081
		URI fileUri = file.toURI();
		URI parentUri = parent.toURI();
		// constructor of File only allows absolute uris, therefore convert from string
		return new File(parentUri.relativize(fileUri).getPath());
	}

}
