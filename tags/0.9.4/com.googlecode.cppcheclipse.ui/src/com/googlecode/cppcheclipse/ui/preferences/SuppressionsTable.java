package com.googlecode.cppcheclipse.ui.preferences;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.ProblemProfile;
import com.googlecode.cppcheclipse.core.Suppression;
import com.googlecode.cppcheclipse.core.SuppressionProfile;
import com.googlecode.cppcheclipse.ui.Console;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.actions.Builder;
import com.googlecode.cppcheclipse.ui.marker.ProblemReporter;

public class SuppressionsTable extends TableEditor {

	private ProblemProfile problemProfile;
	private final IProject project;

	enum TableColumn {
		Filename(Messages.SuppressionsTable_ColumnFilename, SWT.LEFT, 150), Problem(Messages.SuppressionsTable_ColumnProblem, SWT.LEFT,
				400), Line(Messages.SuppressionsTable_ColumnLine, SWT.LEFT, 50);

		private final String label;
		private final int style;
		private final int width;

		TableColumn(String label, int style, int width) {
			this.label = label;
			this.style = style;
			this.width = width;
		}

		public String getLabel() {
			return label;
		}

		public int getStyle() {
			return style;
		}

		public int getWidth() {
			return width;
		}
	}

	public SuppressionsTable(String name, String labelText, Composite parent,
			IProject project) {
		super(name, labelText, parent, EnumSet.of(TableType.REMOVE,
				TableType.REMOVE_ALL, TableType.ADD));

		getTableViewer(parent).getTable().setHeaderVisible(true);
		getTableViewer(parent).getTable().setLinesVisible(true);
		for (TableColumn column : TableColumn.values()) {
			addColumn(column.getLabel(), column.getStyle(), column.getWidth());
		}

		getTableViewer(parent).setContentProvider(
				new SuppressionsContentProvider());
		getTableViewer(parent)
				.setLabelProvider(new SuppressionsLabelProvider());
		this.project = project;
	}

	@Override
	protected void doLoad() {
		SuppressionProfile profile = new SuppressionProfile(
				getPreferenceStore(), project);
		try {
			problemProfile = CppcheclipsePlugin.getNewProblemProfile(new Console(), getPreferenceStore());
		} catch (Exception e) {
			CppcheclipsePlugin.log(e);
		}
		getTableViewer().setInput(profile);
	}

	@Override
	protected void doLoadDefault() {
		removeAllPressed();
	}

	@Override
	protected void doStore() {
		SuppressionProfile profile = (SuppressionProfile) getTableViewer()
				.getInput();
		try {
			profile.save();
		} catch (IOException e) {
			CppcheclipsePlugin.log(e);
		}

	}

	private class SuppressionsLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String text = ""; //$NON-NLS-1$
			Suppression suppression = (Suppression) element;
			TableColumn column = TableColumn.values()[columnIndex];
			switch (column) {
			case Filename:
				text = suppression.getFile().getName();
				break;
			case Problem:
				if (suppression.isFileSuppression()) {
					text = Messages.SuppressionsTable_AllProblems;
				} else {
					text = problemProfile.getProblemMessage(suppression.getProblemId());
				}
				break;
			case Line:
				if (suppression.isAllLines()) {
					text = Messages.SuppressionsTable_AllLines;
				} else {
					text = String.valueOf(suppression.getLine());
				}
			}
			return text;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

	}

	private class SuppressionsContentProvider implements
			IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			SuppressionProfile profile = (SuppressionProfile) inputElement;
			return profile.getSuppressions().toArray();
		}

	}

	@Override
	protected void addPressed() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				shell, new WorkbenchLabelProvider(),
				new BaseWorkbenchContentProvider());
		dialog.setTitle(Messages.SuppressionsTable_FileSelection);
		dialog.setMessage(Messages.SuppressionsTable_FileSelectionMessage);
		dialog.setInput(project);
		dialog.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IFolder) {
					return true;
				} else if (element instanceof IFile) {
					IFile file = (IFile) element;
					return Builder.shouldCheck(file.getName());
				}
				return false;
			}
		});
		
		dialog.setValidator(new ISelectionStatusValidator() {

			public IStatus validate(Object[] selection) {
				if (selection.length > 1 || selection.length < 1) {
					return new Status(Status.ERROR, CppcheclipsePlugin.getId(),
							Messages.SuppressionsTable_FileSelectionErrorExactlyOne);
				}
				Object element = selection[0];
				if (!(element instanceof IFile)) {
					return new Status(Status.ERROR, CppcheclipsePlugin.getId(),
							Messages.SuppressionsTable_FileSelectionErrorFile);
				}

				return new Status(Status.OK, CppcheclipsePlugin.getId(), ""); //$NON-NLS-1$
			}
		});

		if (dialog.open() == Dialog.OK) {
			IFile file = (IFile) dialog.getFirstResult();
			if (file != null) {
				SuppressionProfile profile = (SuppressionProfile) getTableViewer()
						.getInput();
				Suppression suppression = profile.addFileSuppression(file);
				try {
					new ProblemReporter().deleteMarkers(file);
				} catch (CoreException e) {
					CppcheclipsePlugin.log(e);
				}
				getTableViewer().add(suppression);
			}
		}
	}

	@Override
	protected void removeAllPressed() {
		SuppressionProfile profile = (SuppressionProfile) getTableViewer()
				.getInput();
		profile.removeAllSuppression();
		getTableViewer().refresh();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void removePressed() {
		SuppressionProfile profile = (SuppressionProfile) getTableViewer()
				.getInput();
		for (Iterator iterator = getSelection().iterator(); iterator.hasNext();) {
			Suppression suppression = (Suppression) iterator.next();
			profile.removeSuppression(suppression);
			getTableViewer().remove(suppression);
		}

	}
}
