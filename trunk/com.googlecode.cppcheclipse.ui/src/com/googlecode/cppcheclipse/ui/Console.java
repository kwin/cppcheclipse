package com.googlecode.cppcheclipse.ui;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Wrapper around a console window, which can output an existing InputSteam.
 * 
 * @author Konrad Windszus
 * 
 */
public class Console implements com.googlecode.cppcheclipse.core.IConsole {

	private static final String NAME = Messages.Console_Title;
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
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.core.IConsole#getConsoleOutputStream(boolean)
	 */
	public OutputStream getConsoleOutputStream(boolean isError) {
		final MessageConsoleStream output = console.newMessageStream();
		output.setActivateOnWrite(false);
		
		final int colorId;
		if (!isError) {
			colorId = SWT.COLOR_BLACK;
		} else {
			colorId = SWT.COLOR_RED;
		}
		
		/* we must set the color in the UI thread */
		Runnable runnable = new Runnable() {
			public void run() {
				org.eclipse.swt.graphics.Color color = Display.getCurrent()
						.getSystemColor(colorId);
				output.setColor(color);
			}
		};
		Display.getDefault().syncExec(runnable);
		
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.googlecode.cppcheclipse.command.IConsole#print(java.lang.String)
	 */
	public void print(String line) throws IOException {
		final MessageConsoleStream output = console.newMessageStream();
		output.print(line);
		output.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.googlecode.cppcheclipse.command.IConsole#println(java.lang.String)
	 */
	public void println(String line) throws IOException {
		final MessageConsoleStream output = console.newMessageStream();
		output.println(line);
		output.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.googlecode.cppcheclipse.command.IConsole#show()
	 */
	public void show() throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = (IConsoleView) page.showView(id);
		view.display(console);
	}
}
