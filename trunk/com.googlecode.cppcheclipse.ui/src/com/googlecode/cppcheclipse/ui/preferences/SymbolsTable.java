package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.googlecode.cppcheclipse.core.Symbol;
import com.googlecode.cppcheclipse.core.Symbols;
import com.googlecode.cppcheclipse.ui.ToolchainSettings;

public class SymbolsTable extends TableEditor<Symbols, Symbol> {

	private final ToolchainSettings settings;
	private Button removeButton, removeAllButton, editButton;

	static enum TableColumn {
		Name, Value, CDTDefined, SystemDefined;
	}

	public SymbolsTable(String name, String labelText, Composite parent,
			ToolchainSettings settings) {
		super(name, labelText, parent);

		getTableViewer(parent).getTable().setHeaderVisible(true);
		getTableViewer(parent).getTable().setLinesVisible(true);

		// in the same order as the enum TableColumn
		addColumn(new ExtendedTableColumn("Name", SWT.LEFT, 150));
		addColumn(new ExtendedTableColumn("Value", SWT.LEFT, 350));
		addColumn(new ExtendedTableColumn("CDT", SWT.LEFT, 40));
		addColumn(new ExtendedTableColumn("System", SWT.LEFT, 40));

		getTableViewer(parent).setLabelProvider(new LabelProvider());
		this.settings = settings;
	}

	private class LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String text = ""; //$NON-NLS-1$
			Symbol symbol = (Symbol) element;
			TableColumn column = TableColumn.values()[columnIndex];
			switch (column) {
			case Name:
				text = symbol.getName();
				break;
			case Value:
				text = symbol.getValue();
				break;
			case CDTDefined:
				if (symbol.isCDTDefined()) {
					text = "yes";
				} else {
					text = "no";
				}
				break;
			case SystemDefined:
				if (symbol.isSystemDefined()) {
					text = "yes";
				} else {
					text = "no";
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

	@Override
	protected Symbols createModel() {
		// get predefined symbols
		Symbols symbols = new Symbols(getPreferenceStore(), settings);
		/*
		 * if (includeCDTSystemSymbols.getBooleanValue()) {
		 * symbols.addSymbols(settings.getSymbols(true)); } if
		 * (includeCDTUserSymbols.getBooleanValue()) {
		 * symbols.addSymbols(settings.getSymbols(false)); }
		 */
		return symbols;
	}

	@Override
	protected void createButtons(Composite box) {
		// add symbol
		createPushButton(box, "Add", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPressed();
			}
		});

		// remove symbol
		editButton = createPushButton(box, "Edit", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editPressed();
			}
		});

		// remove symbol
		removeButton = createPushButton(box, "Remove", new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removePressed();
			}
		});

		// remove all
		removeAllButton = createPushButton(box, "Remove All",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						removeAllPressed();
					}
				});
	}
	
	protected void removeAllPressed() {
		getModel().removeUserSymbols();
		getTableViewer().refresh();
	}

	private void addPressed() {
		// open new dialog
		SymbolEditDialog dialog = new SymbolEditDialog(getPage().getShell(),
				"Add new symbol");
		if (dialog.open() == Dialog.OK) {

			getModel().addSymbol(
					new Symbol(dialog.getName(), dialog.getValue()));
			getTableViewer().refresh();
		}
	}

	private void editPressed() {
		// open new dialog
		SymbolEditDialog dialog = new SymbolEditDialog(getPage().getShell(),
				"Edit symbol");
		Symbol symbol = (Symbol) getSelection().getSelection()
				.getFirstElement();
		if (symbol != null) {
			dialog.setName(symbol.getName());
			dialog.setValue(symbol.getValue());
			if (dialog.open() == Dialog.OK) {
				symbol.setName(dialog.getName());
				symbol.setValue(dialog.getValue());
				getTableViewer().refresh();
			}
		}
	}

	protected void selectionChanged(IterableStructuredSelection selection) {
		// check all elements
		// if some is CDT defined disable
		for (Symbol symbol : selection) {
			if (symbol.isCDTDefined()) {
				removeAllButton.setEnabled(false);
				removeButton.setEnabled(false);
				editButton.setEnabled(false);
				return;
			}
			removeAllButton.setEnabled(true);
			removeButton.setEnabled(true);
			editButton.setEnabled(true);
		}
	}
}
