package com.googlecode.cppcheclipse.ui.preferences;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.ProblemProfile;
import com.googlecode.cppcheclipse.core.Suppression;
import com.googlecode.cppcheclipse.core.SuppressionProfile;
import com.googlecode.cppcheclipse.ui.Console;
import com.googlecode.cppcheclipse.ui.Messages;
import com.googlecode.cppcheclipse.ui.marker.ProblemReporter;

public class SuppressionsTable extends
		TableEditor<SuppressionProfile, Suppression> {

	private ProblemProfile problemProfile;
	private final IProject project;

	static enum TableColumn {
		Filename(Messages.SuppressionsTable_ColumnFilename, SWT.LEFT, 150), Problem(
				Messages.SuppressionsTable_ColumnProblem, SWT.LEFT, 400), Line(
				Messages.SuppressionsTable_ColumnLine, SWT.LEFT, 50);

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
		super(name, labelText, parent);

		getTableViewer(parent).getTable().setHeaderVisible(true);
		getTableViewer(parent).getTable().setLinesVisible(true);
		for (TableColumn column : TableColumn.values()) {
			addColumn(column.getLabel(), column.getStyle(), column.getWidth());
		}

		getTableViewer(parent).setContentProvider(new ContentProvider());
		getTableViewer(parent).setLabelProvider(new LabelProvider());
		this.project = project;
	}

	@Override
	protected void doLoad() {
		SuppressionProfile profile = new SuppressionProfile(
				getPreferenceStore(), project);
		try {
			problemProfile = CppcheclipsePlugin.getNewProblemProfile(
					new Console(), getPreferenceStore());
		} catch (Exception e) {
			CppcheclipsePlugin.log(e);
		}
		setModel(profile);
	}

	@Override
	protected void doLoadDefault() {
		removeAllPressed();
	}

	@Override
	protected void doStore() {
		SuppressionProfile profile = getModel();
		try {
			profile.save();
		} catch (IOException e) {
			CppcheclipsePlugin.log(e);
		}

	}

	private class LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String text = ""; //$NON-NLS-1$
			Suppression suppression = (Suppression) element;
			TableColumn column = TableColumn.values()[columnIndex];
			switch (column) {
			case Filename:
				text = suppression.getFile(false).toString();
				break;
			case Problem:
				if (suppression.isFileSuppression()) {
					text = Messages.SuppressionsTable_AllProblems;
				} else {
					text = problemProfile.getProblemMessage(suppression
							.getProblemId());
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

	private static class ContentProvider implements IStructuredContentProvider {

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
	protected void createButtons(Composite box) {
		createPushButton(box, Messages.TableEditor_Add, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPressed();
			}
		});
		createPushButton(box, Messages.TableEditor_AddExternal,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						addExternalPressed();
					}
				});
		createPushButton(box, Messages.TableEditor_Remove,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						removePressed();
					}
				});
		createPushButton(box, Messages.TableEditor_RemoveAll,
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						removeAllPressed();
					}
				});
	}

	protected void addPressed() {
		IFile file = openProjectFile(Messages.SuppressionsTable_FileSelection,
				Messages.SuppressionsTable_FileSelectionMessage, project);
		if (file != null) {
			SuppressionProfile profile = getModel();
			Suppression suppression = profile.addFileSuppression(file
					.getProjectRelativePath().toFile());
			try {
				new ProblemReporter().deleteMarkers(file, true);
			} catch (CoreException e) {
				CppcheclipsePlugin.log(e);
			}
			getTableViewer().add(suppression);
		}
	}

	protected void addExternalPressed() {
		File file = openExternalFile(Messages.SuppressionsTable_FileSelection);
		if (file != null) {
			SuppressionProfile profile = getModel();
			Suppression suppression = profile.addFileSuppression(file);
			try {
				new ProblemReporter().deleteMarkers(project, false);
			} catch (CoreException e) {
				CppcheclipsePlugin.log(e);
			}
			getTableViewer().add(suppression);
		}
	}

	protected void removeAllPressed() {
		SuppressionProfile profile = getModel();
		profile.removeAllSuppression();
		getTableViewer().refresh();
	}

	protected void removePressed() {
		SuppressionProfile profile = getModel();
		for (Suppression suppression : getSelection()) {
			profile.removeSuppression(suppression);
			getTableViewer().remove(suppression);
		}

	}
}
