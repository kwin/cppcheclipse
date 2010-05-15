package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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

public abstract class TableEditor extends FieldEditor {
	
	private TableViewer tableViewer;
	private Composite buttonBox;
	
	public TableEditor(String name, String labelText, Composite parent) {
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
            table.addSelectionListener(new SelectionAdapter() {
            	@Override
            	public void widgetSelected(SelectionEvent e) {
            		selectionChanged();
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
    abstract protected void createButtons(Composite box);
	
	 /**
     * Helper method to create a push button.
     * 
     * @param parent the parent control
     * @param key the resource name used to supply the button's label text
     * @return Button
     */
    protected Button createPushButton(Composite parent, String key, SelectionListener listener) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(JFaceResources.getString(key));
        button.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        int widthHint = convertHorizontalDLUsToPixels(button,
                IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true).x);
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
        // TODO: disable remove button
    	
    }

	public IStructuredSelection getSelection() {
		return (IStructuredSelection)tableViewer.getSelection();
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

}
