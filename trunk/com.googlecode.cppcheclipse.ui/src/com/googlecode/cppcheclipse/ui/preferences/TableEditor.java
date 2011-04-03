package com.googlecode.cppcheclipse.ui.preferences;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.TableModel;
import com.googlecode.cppcheclipse.ui.Builder;
import com.googlecode.cppcheclipse.ui.Messages;

/**
 * 
 * @author Konrad Windszus
 * 
 * @param <Model>
 *            the class of the model which is used to fill this table
 * @param <Element>
 *            the class of the elements within the model (used for selection)
 */
public abstract class TableEditor<Model extends TableModel<Element>, Element>
		extends FieldEditor {

	private TableViewer tableViewer;
	private Composite buttonBox;
	private int columnIndex;

	/**
	 * models one table column with its label, style (alignment) and width
	 * 
	 */
	public static class ExtendedTableColumn {

		private final String label;
		private final int style;
		private final int width;

		/**
		 * Constructor
		 * 
		 * @param label
		 *            the label of the column
		 * @param style
		 *            the style, see overview of TableColumn
		 * @param width
		 *            the width in pixels
		 * @see TableColumn
		 */
		ExtendedTableColumn(String label, int style, int width) {
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

	private class ContentProvider implements IStructuredContentProvider {
		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			@SuppressWarnings("unchecked")
			Model model = (Model) inputElement;
			return model.toArray();
		}

	}
	
	// taken from http://eclipsisms.blogspot.com/2007/07/tableviewer-sorting-using-labelprovider.html
	private static class TableColumnSorter extends ViewerComparator {
        // TODO: switch to enum
		public static final int ASC = 1;

        public static final int NONE = 0;

        public static final int DESC = -1;

        private int direction = 0;

        private final TableColumn column;

        private final TableViewer viewer;
        
        private final int index;

        public TableColumnSorter(TableViewer viewer, TableColumn column, int index) {
                this.column = column;
                this.viewer = viewer;
                this.index = index;
                this.column.addSelectionListener(new SelectionAdapter() {
                        public void widgetSelected(SelectionEvent e) {
                                if (TableColumnSorter.this.viewer.getComparator() != null) {
                                        if (TableColumnSorter.this.viewer.getComparator() == TableColumnSorter.this) {
                                                int tdirection = TableColumnSorter.this.direction;

                                                if (tdirection == ASC) {
                                                        setSorter(TableColumnSorter.this, DESC);
                                                } else if (tdirection == DESC) {
                                                        setSorter(TableColumnSorter.this, NONE);
                                                }
                                        } else {
                                                setSorter(TableColumnSorter.this, ASC);
                                        }
                                } else {
                                        setSorter(TableColumnSorter.this, ASC);
                                }
                        }
                });
        }

        public void setSorter(TableColumnSorter sorter, int direction) {
                if (direction == NONE) {
                        column.getParent().setSortColumn(null);
                        column.getParent().setSortDirection(SWT.NONE);
                        viewer.setComparator(null);
                } else {
                        column.getParent().setSortColumn(column);
                        sorter.direction = direction;

                        if (direction == ASC) {
                                column.getParent().setSortDirection(SWT.DOWN);
                        } else {
                                column.getParent().setSortDirection(SWT.UP);
                        }

                        if (viewer.getComparator() == sorter) {
                                viewer.refresh();
                        } else {
                                viewer.setComparator(sorter);
                        }

                }
        }

        public int compare(Viewer viewer, Object e1, Object e2) {
                return direction * doCompare(viewer, e1, e2);
        }

        /**
         * Overwrite this compare method if text comparison isn't what you need
         * @param TableViewer
         * @param e1
         * @param e2
         * @return
         */
        protected int doCompare(Viewer TableViewer, Object e1, Object e2) {
        	ITableLabelProvider lp = ((ITableLabelProvider) viewer
                .getLabelProvider());
                String t1 = lp.getColumnText(e1, index);
                String t2 = lp.getColumnText(e2, index);
                return t1.compareTo(t2);
        }
}

	public TableEditor(String name, String labelText, Composite parent) {
		columnIndex = 0;
		
		// imitate behaviour of superclass, can't call it directly, because then
		// the members would not have been correctly initialized
		init(name, labelText);
		createControl(parent);
		getTableViewer(parent).setContentProvider(new ContentProvider());
	}
	
	/**
	 * Should instantiate a new model.
	 * @return the model which was instantiated
	 */
	protected abstract Model createModel();
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) tableViewer.getTable().getLayoutData()).horizontalSpan = numColumns - 1;
	}

	public void addColumn(ExtendedTableColumn column) {
		TableColumn tc = new TableColumn(tableViewer.getTable(),
				column.getStyle());
		tc.setText(column.getLabel());
		tc.setWidth(column.getWidth());
		tc.setMoveable(true);
		
		new TableColumnSorter(tableViewer, tc, columnIndex++);
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		tableViewer = getTableViewer(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		tableViewer.getTable().setLayoutData(gd);

		buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list control
	 */
	public TableViewer getTableViewer(Composite parent) {
		if (tableViewer == null) {
			tableViewer = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL
					| SWT.H_SCROLL | SWT.FULL_SELECTION);
			Table table = tableViewer.getTable();
			table.setFont(parent.getFont());
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectionChanged(getSelection());
					super.widgetSelected(e);
				}
			});
			

		} else {
			checkParent(tableViewer.getTable(), parent);
		}
		return tableViewer;
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	/**
	 * Returns this field editor's button box containing the Add, Remove, Up,
	 * and Down button.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
		} else {
			checkParent(buttonBox, parent);
		}
		selectionChanged(getSelection());
		return buttonBox;
	}

	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 * 
	 * @param box
	 *            the box for the buttons
	 */
	abstract protected void createButtons(Composite box);

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent
	 *            the parent control
	 * @param key
	 *            the resource name used to supply the button's label text
	 * @return Button
	 */
	protected Button createPushButton(Composite parent, String key,
			SelectionListener listener) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceResources.getString(key));
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(listener);
		return button;
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getTableViewer(parent).getTable().setEnabled(enabled);
		for (Control control : buttonBox.getChildren()) {
			control.setEnabled(enabled);
		}
		buttonBox.setEnabled(enabled);
	}
	
	@Override
	protected void doStore() {
		Model model = getModel();
		try {
			model.save();
		} catch (IOException e) {
			CppcheclipsePlugin.logError("Could not save table preferences", e);
		}
	}

	/**
	 * Default method for do loads, calls createModel.
	 */
	@Override
	protected void doLoad() {
		// create the model instance
		Model model = createModel();
		setModel(model);
	}
	
	@Override
	protected void doLoadDefault() {
		removeAllPressed();
	}

	/**
	 * Invoked when the selection in the list has changed.
	 * 
	 * <p>
	 * The default implementation of this method utilizes the selection index
	 * and the size of the list to toggle the enablement of the up, down and
	 * remove buttons.
	 * </p>
	 * 
	 * <p>
	 * Sublcasses may override.
	 * </p>
	 * @param iterableStructuredSelection 
	 * 
	 * @since 3.5
	 */
	protected void selectionChanged(IterableStructuredSelection iterableStructuredSelection) {
		// TODO: disable remove button

	}

	public IterableStructuredSelection getSelection() {
		return new IterableStructuredSelection(tableViewer.getSelection());
	}

	public class IterableStructuredSelection implements Iterable<Element> {

		private final IStructuredSelection selection;

		public IterableStructuredSelection(ISelection selection) {
			this.selection = (IStructuredSelection) selection;
		}

		@SuppressWarnings("unchecked")
		public Iterator<Element> iterator() {
			return (Iterator<Element>) selection.iterator();
		}

		public IStructuredSelection getSelection() {
			return selection;
		}

	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * default button handling to remove all elements
	 */
	protected void removeAllPressed() {
		getModel().removeAll();
		getTableViewer().refresh();
	}

	/**
	 * default button handling to remove one element
	 */
	protected void removePressed() {
		Model model = getModel();
		for (Element element : getSelection()) {
			model.remove(element);
			getTableViewer().remove(element);
		}
	}

	protected File openExternalFile(String title) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		FileDialog fileDialog = new FileDialog(shell);
		fileDialog.setText(title);
		String file = fileDialog.open();
		if (file != null) {
			return new File(file);
		}
		return null;
	}

	
	protected void setModel(Model model) {
		getTableViewer().setInput(model);
	}

	/**
	 * 
	 * @return the model used by this editor (obtained from table viewer input)
	 */
	@SuppressWarnings("unchecked")
	protected Model getModel() {
		return (Model) getTableViewer().getInput();
	}

	protected IFile openProjectFile(String title, String message,
			IProject project) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				shell, new WorkbenchLabelProvider(),
				new BaseWorkbenchContentProvider());
		dialog.setTitle(title);
		dialog.setMessage(message);
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
							Messages.TableEditor_FileSelectionErrorExactlyOne);
				}
				Object element = selection[0];
				if (!(element instanceof IFile)) {
					return new Status(Status.ERROR, CppcheclipsePlugin.getId(),
							Messages.TableEditor_FileSelectionErrorFile);
				}

				return new Status(Status.OK, CppcheclipsePlugin.getId(), ""); //$NON-NLS-1$
			}
		});

		if (dialog.open() == Dialog.OK) {
			return (IFile) dialog.getFirstResult();
		}
		return null;
	}
}
