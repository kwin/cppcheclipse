package com.googlecode.cppcheclipse.ui.preferences;

import java.util.EnumSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;

import com.googlecode.cppcheclipse.ui.Messages;

public abstract class TableEditor extends FieldEditor {
	
	enum TableType { ADD, REMOVE, REMOVE_ALL };
	
	private final EnumSet<TableType> tableType;
	private TableViewer tableViewer;
	private Composite buttonBox;
	private Button addButton, removeButton, removeAllButton;
	private SelectionListener selectionListener;
	
	public TableEditor(String name, String labelText, Composite parent, EnumSet <TableType> tableType) {
		this.tableType = tableType;
		// imitate behaviour of superclass, can't call it directly, because then the members would not have been correctly initialized
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
        ((GridData) control.getLayoutData()).horizontalSpan = numColumns;
        ((GridData) tableViewer.getTable().getLayoutData()).horizontalSpan = numColumns - 1;
	}
	
	public void addColumn(String label, int style, int width) {
		// Add the first name column
	    TableColumn tc = new TableColumn(tableViewer.getTable(), style);
	    tc.setText(label);
	    tc.setWidth(width);
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
     * @param parent the parent control
     * @return the list control
     */
    public TableViewer getTableViewer(Composite parent) {
        if (tableViewer == null) {
            tableViewer = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
            Table table = tableViewer.getTable();
            table.setFont(parent.getFont());
            table.addSelectionListener(getSelectionListener());
            /*
            list.addSelectionListener(getSelectionListener());
            list.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    list = null;
                }
            });*/
        } else {
            checkParent(tableViewer.getTable(), parent);
        }
        return tableViewer;
    }
    
    public TableViewer getTableViewer() {
    	return tableViewer;
    }
	
    /**
     * Returns this field editor's button box containing the Add, Remove,
     * Up, and Down button.
     *
     * @param parent the parent control
     * @return the button box
     */
    public Composite getButtonBoxControl(Composite parent) {
        if (buttonBox == null) {
            buttonBox = new Composite(parent, SWT.NULL);
            GridLayout layout = new GridLayout();
            layout.marginWidth = 0;
            buttonBox.setLayout(layout);
            createButtons(buttonBox);
            buttonBox.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    addButton = null;
                    removeButton = null;
                    removeAllButton = null;
                    buttonBox = null;
                }
            });

        } else {
            checkParent(buttonBox, parent);
        }

        selectionChanged();
        return buttonBox;
    }
    
	/**
     * Creates the Add, Remove, Up, and Down button in the given button box.
     *
     * @param box the box for the buttons
     */
    private void createButtons(Composite box) {
        if (tableType.contains(TableType.ADD))
        	addButton = createPushButton(box, Messages.TableEditor_Add);
        if (tableType.contains(TableType.REMOVE))
        	removeButton = createPushButton(box, Messages.TableEditor_Remove);
        if (tableType.contains(TableType.REMOVE_ALL))
        	removeAllButton = createPushButton(box, Messages.TableEditor_RemoveAll);
    } 
	
	 /**
     * Helper method to create a push button.
     * 
     * @param parent the parent control
     * @param key the resource name used to supply the button's label text
     * @return Button
     */
    private Button createPushButton(Composite parent, String key) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(JFaceResources.getString(key));
        button.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        int widthHint = convertHorizontalDLUsToPixels(button,
                IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        button.addSelectionListener(getSelectionListener());
        return button;
    }
    
    /*
     * @see FieldEditor.setEnabled(boolean,Composite).
     */
    public void setEnabled(boolean enabled, Composite parent) {
        super.setEnabled(enabled, parent);
        getTableViewer(parent).getTable().setEnabled(enabled);
        if (addButton != null)
        	addButton.setEnabled(enabled);
        if (removeButton != null)
        	removeButton.setEnabled(enabled);
        if (removeAllButton != null)
        	removeAllButton.setEnabled(enabled);
    }
    
    /**
     * Returns this field editor's selection listener.
     * The listener is created if nessessary.
     *
     * @return the selection listener
     */
    private SelectionListener getSelectionListener() {
        if (selectionListener == null) {
			createSelectionListener();
		}
        return selectionListener;
    }
    
    /**
     * Creates a selection listener.
     */
    public void createSelectionListener() {
        selectionListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Widget widget = event.widget;
                if (widget == addButton) {
                    addPressed();
                } else if (widget == removeButton) {
                    removePressed();
                } else if (widget == removeAllButton) {
                    removeAllPressed();
                } else if (widget == tableViewer.getTable()) {
                    selectionChanged();
                }
            }
        };
    }
    
    protected abstract void addPressed();
    protected abstract void removeAllPressed();
    protected abstract void removePressed();
    
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
	 * 
	 * @since 3.5
	 */
    protected void selectionChanged() {

        int index = tableViewer.getTable().getSelectionIndex();
        //int size = tableViewer.getTable().getItemCount();

        
        if (removeButton != null)
        	removeButton.setEnabled(index >= 0);
    }

	public IStructuredSelection getSelection() {
		return (IStructuredSelection)tableViewer.getSelection();
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

}
