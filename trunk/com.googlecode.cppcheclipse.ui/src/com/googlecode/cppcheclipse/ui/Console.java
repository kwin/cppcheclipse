package com.googlecode.cppcheclipse.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.UIJob;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.utils.PatternSearch;

/**
 * Wrapper around a console window, which can output an existing InputSteam.
 * @author Konrad Windszus
 * @TODO: move to UI plugin
 *
 */
public class Console implements com.googlecode.cppcheclipse.core.IConsole {

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
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	
	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.command.IConsole#createByteArrayOutputStream(boolean)
	 */
	public ByteArrayOutputStream createByteArrayOutputStream(boolean isError) {
		int colorId;
		if (!isError) {
			colorId = SWT.COLOR_BLACK;
		} else {
			colorId = SWT.COLOR_RED;
		}
		return new ConsoleByteArrayOutputStream(colorId);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.command.IConsole#print(java.lang.String)
	 */
	public void print(String line) throws IOException {
		final MessageConsoleStream output = console.newMessageStream();
		output.print(line);
		output.close();
	}
	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.command.IConsole#println(java.lang.String)
	 */
	public void println(String line) throws IOException {
		final MessageConsoleStream output = console.newMessageStream();
		output.println(line);
		output.close();
	}

	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.command.IConsole#show()
	 */
	public void show() throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = (IConsoleView) page.showView(id);
		view.display(console);
	}
	
	private class ConsoleByteArrayOutputStream extends ByteArrayOutputStream {
		private final MessageConsoleStream output;
		private final ByteArrayOutputStream consoleBuffer;
		
		private static final int BYTE_ARRAY_INITIAL_SIZE = 4096;
		
		public ConsoleByteArrayOutputStream(int colorId) {
			this(BYTE_ARRAY_INITIAL_SIZE, colorId);
		}
		
		public ConsoleByteArrayOutputStream(int size, final int colorId) {
			super(size);
			output = console.newMessageStream();
			output.setActivateOnWrite(false);
			consoleBuffer = new ByteArrayOutputStream();
			/* we must set the color in the UI thread, but if we are already in the UI thread scheduling the job leads to deadlocks */
			
			UIJob job = new UIJob("Setting color of console")
			{
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					org.eclipse.swt.graphics.Color color = Display.getCurrent().getSystemColor(colorId);
					output.setColor(color);
					return Status.OK_STATUS;
				}	
			};
			
			// either execute job in UI thread if we are not already within the UI thread
			if (Display.getCurrent() == null) {
				job.schedule();
				try {
					job.join();
				} catch (InterruptedException e) {
					CppcheclipsePlugin.log(e);
				}
			} else {
				job.runInUIThread(new NullProgressMonitor());
			}
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			super.write(b, off, len);
			consoleBuffer.write(b, off, len);
			flushConsoleBuffer();
		}
		
		private boolean flushConsoleBuffer() {
			byte[] buffer = consoleBuffer.toByteArray();
			int pos = PatternSearch.indexAfterLinebreak(buffer);
			if (pos >= 0) {
				consoleBuffer.reset();
				consoleBuffer.write(buffer, pos, buffer.length - pos);
				try {
					output.write(buffer, 0, pos);
				} catch (IOException e) {
					CppcheclipsePlugin.log(e);
				}
			}
			return false;
		}
		
		@Override
		public synchronized void write(int b) {
			super.write(b);
			consoleBuffer.write(b);
			flushConsoleBuffer();
		}

		@Override
		public void close() throws IOException {
			output.write(consoleBuffer.toByteArray());
			super.close();
			output.close();
		}
	}

	/*
	public class ConsoleInputStream extends FilterInputStream {
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
			if (result > 0) {
				output.write(b, off, result);
			}
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
	*/
}
