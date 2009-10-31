package com.googlecode.cppcheclipse.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Utils {

	private Utils() {
		// only has static methods
	}
	
	public static void openUrl(String url) throws PartInitException, MalformedURLException {
		PlatformUI.getWorkbench().getBrowserSupport()
		.getExternalBrowser().openURL(
				new URL(url)); 
	}
}
