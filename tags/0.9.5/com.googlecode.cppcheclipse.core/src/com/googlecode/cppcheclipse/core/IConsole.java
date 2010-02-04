package com.googlecode.cppcheclipse.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.ui.PartInitException;

public interface IConsole {

	public abstract ByteArrayOutputStream createByteArrayOutputStream(
			boolean isError);

	public abstract void print(String line) throws IOException;

	public abstract void println(String line) throws IOException;

	public abstract void show() throws PartInitException;

}