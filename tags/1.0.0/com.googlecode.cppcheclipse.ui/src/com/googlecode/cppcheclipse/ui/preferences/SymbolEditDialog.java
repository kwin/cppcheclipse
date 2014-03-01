package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Edit dialog for symbols with two text fields
 * name and value.
 * Derived from InputDialog which only provides one input text field
 */
public class SymbolEditDialog extends Dialog {

	private final String title;
	private Text nameControl, valueControl;
	private String name, value;
	
	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SymbolEditDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
		this.name = "";	// default values
		this.value = "";
	}
	
	protected Control createDialogArea(Composite parent) {
		// create new parent
		parent = (Composite)super.createDialogArea(parent);
		
		// create a composite with 2 column layout
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// create new label and name fields
		Label label = new Label(composite, SWT.WRAP);
        label.setText("Name");
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER);
        //data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());
        
        nameControl = new Text(composite, SWT.SINGLE | SWT.BORDER);
        nameControl.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
	            | GridData.HORIZONTAL_ALIGN_FILL));
        nameControl.setText(name);
        nameControl.addModifyListener(new ModifyListener() {
	        public void modifyText(ModifyEvent e) {
	            validateInput();
	        }
	    });
		
		
        label = new Label(composite, SWT.WRAP);
        label.setText("Value");
        data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_CENTER);
        //data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        label.setLayoutData(data);
        label.setFont(parent.getFont());
        
        valueControl = new Text(composite, SWT.SINGLE | SWT.BORDER);
        valueControl.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
	            | GridData.HORIZONTAL_ALIGN_FILL));
        valueControl.setText(value);
        valueControl.addModifyListener(new ModifyListener() {
	        public void modifyText(ModifyEvent e) {
	            //validateInput();
	        }
	    });
		
		// TODO:  create new label and value fields
	    return parent;
	}
	
	private void validateInput() {
		
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}
	
	public String getName() {
		return name;
	}
	
	
	public String getValue() {
		return value;
	}
	
	
	 /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
    	// need to persist the values, since they are inaccessible after the dialog is destroyed
        if (buttonId == IDialogConstants.OK_ID) {
            name = nameControl.getText();
            value = valueControl.getText();
        } else {
        	name = null;
            value = null;
        }
        super.buttonPressed(buttonId);
    }
	
	/*@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
	    Composite composite = (Composite) super.createDialogArea(parent);
	    // create message
	    if (message != null) {
	        Label label = new Label(composite, SWT.WRAP);
	        label.setText(message);
	        GridData data = new GridData(GridData.GRAB_HORIZONTAL
	                | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
	                | GridData.VERTICAL_ALIGN_CENTER);
	        data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
	        label.setLayoutData(data);
	        label.setFont(parent.getFont());
	    }
	    text = new Text(composite, getInputTextStyle());
	    text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
	            | GridData.HORIZONTAL_ALIGN_FILL));
	    text.addModifyListener(new ModifyListener() {
	        public void modifyText(ModifyEvent e) {
	            validateInput();
	        }
	    });
	    errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
	    errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
	            | GridData.HORIZONTAL_ALIGN_FILL));
	    errorMessageText.setBackground(errorMessageText.getDisplay()
	            .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    // Set the error message text
	    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
	    setErrorMessage(errorMessage);

	    applyDialogFont(composite);
	}*/
	
	

}
