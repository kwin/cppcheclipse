package com.googlecode.cppcheclipse.command;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console {

	private static final String NAME = "cppcheck";
	private final MessageConsole console;
	
	
	public Console() {
		console = findConsole(NAME);
	}
	
	private static MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	}
	
	public ConsoleInputStream createInputStream(InputStream input) {
		return new ConsoleInputStream(input);
	}
	
	public void print(String line) throws IOException {
		final MessageConsoleStream output = console.newMessageStream();
		output.println(line);
		output.close();
	}
	
	
	public class ConsoleInputStream extends FilterInputStream{
		private final MessageConsoleStream output;
		public ConsoleInputStream(InputStream input) {
			super(input);
			output = console.newMessageStream();
		}

		@Override
		public int read() throws IOException {
			int result = super.read();
			if (result != -1) {
				output.write(result);
			}
			return result;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int result = super.read(b, off, len);
			output.write(b, off, result);
			return result;
		}
		
		public void print(String line) {
			output.println(line);
		}

		@Override
		public void close() throws IOException {
			super.close();
			output.close();
		}
	}
}
