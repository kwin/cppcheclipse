package com.googlecode.cppcheclipse.ui.preferences;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.googlecode.cppcheclipse.core.Appendages;
import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.ui.Messages;

public class AppendageTable extends TableEditor<Appendages, File> {

	private final IProject project;
	
	static enum TableColumn {
		Filename
	};

	public AppendageTable(String name, String labelText, Composite parent,
			IProject project) {
		super(name, labelText, parent);

		getTableViewer(parent).getTable().setHeaderVisible(true);
		getTableViewer(parent).getTable().setLinesVisible(true);
		addColumn(new ExtendedTableColumn(Messages.AppendageTable_ColumnFile, SWT.LEFT, 150));

		getTableViewer(parent).setContentProvider(new ContentProvider());
		getTableViewer(parent).setLabelProvider(new LabelProvider());
		this.project = project;
	}

	@Override
	protected void doLoad() {
		Appendages appendages = new Appendages(getPreferenceStore());
		setModel(appendages);
	}

	@Override
	protected void doLoadDefault() {
		removeAllPressed();
	}

	@Override
	protected void doStore() {
		try {
			getModel().save();
		} catch (IOException e) {
			CppcheclipsePlugin.log(e);
		}
	}

	private static class LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String text = ""; //$NON-NLS-1$
			TableColumn column = TableColumn.values()[columnIndex];
			switch (column) {
			case Filename:
				text = ((File) element).toString();
				break;
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
			// input element is StringList
			Appendages appendages = (Appendages) inputElement;
			return appendages.toArray();
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
		IFile resource = openProjectFile(Messages.AppendageTable_FileSelection, Messages.AppendageTable_FileSelectionMessage, project);
		if (resource != null) {
			File file = resource.getProjectRelativePath().toFile();
			getModel().add(file);
			getTableViewer().add(file);
		}
	}

	protected void addExternalPressed() {
		File file = openExternalFile(Messages.AppendageTable_FileSelection);
		if (file != null) {
			getModel().add(file);
			getTableViewer().add(file);
		}
	}

	protected void removeAllPressed() {
		getModel().clear();
		getTableViewer().refresh();
	}

	protected void removePressed() {
		for (File file : getSelection()) {
			getModel().remove(file);
			getTableViewer().remove(file);
		}

	}
}
