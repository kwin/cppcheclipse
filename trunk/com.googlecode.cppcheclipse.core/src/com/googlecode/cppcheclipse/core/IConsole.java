package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.io.OutputStream;

public interface IConsole {

	public abstract OutputStream getConsoleOutputStream(boolean isError);

	public abstract void print(String line) throws IOException;

	public abstract void println(String line) throws IOException;

	/**
	 * Shows the console view (in an async way, this method may be called from non UI-thread)
	 */
	public abstract void show();

}