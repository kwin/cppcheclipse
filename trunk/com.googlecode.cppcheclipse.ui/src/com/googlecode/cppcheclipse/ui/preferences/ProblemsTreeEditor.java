/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package com.googlecode.cppcheclipse.ui.preferences;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.googlecode.cppcheclipse.core.CppcheclipsePlugin;
import com.googlecode.cppcheclipse.core.PreferenceConstants;
import com.googlecode.cppcheclipse.core.Problem;
import com.googlecode.cppcheclipse.core.ProblemProfile;
import com.googlecode.cppcheclipse.core.ProblemSeverity;

public class ProblemsTreeEditor extends CheckedTreeEditor {

	/*
	 * public ProblemsTreeEditor() { super(); }
	 */

	/**
	 * This ICheckProvider gives information about what items should be checked
	 * and what not
	 * 
	 * @author kwindszus
	 * 
	 */
	class ProblemsCheckStateProvider implements ICheckStateProvider {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang
		 * .Object)
		 */
		public boolean isChecked(Object element) {
			if (element instanceof Problem) {
				Problem p = (Problem) element;
				return p.isEnabled();
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.
		 * Object)
		 */
		public boolean isGrayed(Object element) {
			/*
			 * if (element instanceof IProblem) { return false; } if (element
			 * instanceof IProblemCategory) { // checked if at least one is
			 * checked (buy grayed) IProblemCategory p = (IProblemCategory)
			 * element; Object[] children = p.getChildren(); boolean all_checked
			 * = true; boolean all_unchecked = true; for (int i = 0; i <
			 * children.length; i++) { Object object = children[i]; if
			 * (isChecked(object)) { all_unchecked = false; } else { all_checked
			 * = false; } } if (all_checked || all_unchecked) return false;
			 * return true; }
			 */
			return false;
		}
	}

	class ProblemsContentProvider implements IContentProvider,
			ITreeContentProvider {

		private ProblemProfile profile;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// TODO Auto-generated method stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
		 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			profile = (ProblemProfile) newInput;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang
		 * .Object)
		 */
		public Object[] getChildren(Object parentElement) {
			Object[] children = null;
			if (parentElement instanceof ProblemProfile) {
				children = profile.getCategories().toArray();
			} else if (parentElement instanceof String) {
				children = profile
						.getProblemsOfCategory((String) parentElement).values()
						.toArray();
			}

			if (children == null) {
				children = new Object[0];
			}
			return children;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang
		 * .Object)
		 */
		public Object getParent(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang
		 * .Object)
		 */
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(
		 * java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.googlecode.cppcheclipse.ui.CheckedTreeEditor#checkStateChanged(org
	 * .eclipse.jface.viewers.CheckStateChangedEvent)
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		// in case of changed category, go to all problems
		if (element instanceof String) {
			ITreeContentProvider contentProvider = (ITreeContentProvider)getTreeViewer().getContentProvider();
			Object[] problems = contentProvider.getChildren(element);
			for (Object problem : problems) {
				if (problem instanceof Problem) {
					((Problem)problem).setEnabled(event.getChecked());
				}
			}
		}
		
		if (element instanceof Problem) {
			((Problem) element).setEnabled(event.getChecked());
		}

	}

	public ProblemsTreeEditor(Composite parent) {
		super(PreferenceConstants.P_PROBLEMS_PREFIX, "Problems", parent);

		setEmptySelectionAllowed(true);
		getTreeViewer().getTree().setHeaderVisible(true);
		// getTreeViewer().getTree().
		getTreeViewer().setContentProvider(new ProblemsContentProvider());
		getTreeViewer().setCheckStateProvider(new ProblemsCheckStateProvider());
		// column Name
		TreeViewerColumn column1 = new TreeViewerColumn(getTreeViewer(),
				SWT.NONE);
		column1.getColumn().setWidth(300);
		column1.getColumn().setText("Name");
		column1.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof Problem) {
					Problem p = (Problem) element;
					return p.getMessage();
				} else {
					return element.toString();
				}
			}
		});

		// column Severity
		TreeViewerColumn column2 = new TreeViewerColumn(getTreeViewer(),
				SWT.NONE);
		column2.getColumn().setWidth(100);
		column2.getColumn().setText("Severity");
		column2.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof Problem) {
					Problem p = (Problem) element;
					return p.getSeverity().toString();
				}
				return null;
			}
		});
		column2.setEditingSupport(new EditingSupport(getTreeViewer()) {
			protected boolean canEdit(Object element) {
				return element instanceof Problem;
			}

			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(getTreeViewer().getTree(),
						ProblemSeverity.stringValues());
			}

			protected Object getValue(Object element) {
				return ((Problem) element).getSeverity().ordinal();
			}

			protected void setValue(Object element, Object value) {
				int index = ((Integer) value).intValue();
				ProblemSeverity val = ProblemSeverity.values()[index];
				((Problem) element).setSeverity(val);
				getTreeViewer().update(element, null);
			}
		});

		getTreeViewer().setAutoExpandLevel(2);
		// codanPreferencesLoader.setInput(profile);
		// getViewer().setInput(profile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		if (getTreeControl() != null) {
			ProblemProfile profile;
			try {
				profile = new ProblemProfile(getPreferenceStore());
			} catch (Exception e) {
				CppcheclipsePlugin.log(e);
				profile = new ProblemProfile();
			}
			loadProfile(profile);
		}
	}

	public void loadProfile(ProblemProfile profile) {
		getViewer().setInput(profile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.googlecode.cppcheclipse.ui.CheckedTreeEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		/*
		 * if (getTreeControl() != null) { IProblem[] probs =
		 * codanPreferencesLoader.getProblems(); for (int i = 0; i <
		 * probs.length; i++) { String id = probs[i].getId(); String s =
		 * getPreferenceStore().getDefaultString(id);
		 * codanPreferencesLoader.setProperty(id, s); }
		 * getViewer().setInput(codanPreferencesLoader.getInput()); }
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#doStore()
	 */
	@Override
	protected void doStore() {
		if (getTreeControl() != null) {
			ProblemProfile profile = (ProblemProfile) getViewer().getInput();
			profile.saveToPreferences(getPreferenceStore());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#
	 * modelFromString(java.lang.String)
	 */
	@Override
	protected Object modelFromString(String s) {
		return null;// profile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.CheckedTreeEditor#modelToString
	 * (java.lang.Object)
	 */
	@Override
	protected String modelToString(Object model) {
		return "";
	}
}
