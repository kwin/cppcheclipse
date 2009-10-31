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

import java.util.Collection;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
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
import com.googlecode.cppcheclipse.ui.Console;
import com.googlecode.cppcheclipse.ui.Messages;

public class ProblemsTreeEditor extends CheckedTreeEditor {

	
	class ProblemsContentProvider implements IContentProvider,
			ITreeContentProvider {

		private ProblemProfile profile;
		

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
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
				Collection<Problem> problems = profile.getProblemsOfCategory((String) parentElement);
				children = problems.toArray();
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
		super(PreferenceConstants.P_PROBLEMS, Messages.ProblemsTreeEditor_Problems, parent);

		setEmptySelectionAllowed(true);
		getTreeViewer().getTree().setHeaderVisible(true);
		getTreeViewer().setContentProvider(new ProblemsContentProvider());
		// column Name
		TreeViewerColumn column1 = new TreeViewerColumn(getTreeViewer(),
				SWT.NONE);
		column1.getColumn().setWidth(300);
		column1.getColumn().setText(Messages.ProblemsTreeEditor_Name);
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
		column2.getColumn().setText(Messages.ProblemsTreeEditor_Severity);
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
	}
	
	
	private void loadCheckState(ProblemProfile profile) {
		for(Problem problem : profile.getAllProblems()) {
			getTreeViewer().setChecked(problem, problem.isEnabled());
		}
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
				profile = CppcheclipsePlugin.getNewProblemProfile(new Console(), getPreferenceStore());
				profile.addBinaryChangeListener(new IPropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent event) {
						loadProfile((ProblemProfile)getViewer().getInput());
					}
					
				});
			} catch (Exception e) {
				showErrorMessage(e.getLocalizedMessage());
				CppcheclipsePlugin.log(e);
				profile = null;
				
			}
			loadProfile(profile);
		}
	}

	public void loadProfile(ProblemProfile profile) {
		getViewer().setInput(profile);
		if (profile != null) {
			loadCheckState(profile);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.googlecode.cppcheclipse.ui.CheckedTreeEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		ProblemProfile profile = (ProblemProfile)getViewer().getInput();
		if (profile != null) {
			profile.loadDefaults(getPreferenceStore());
			loadProfile(profile);
		}
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
			if (profile != null) {
				profile.saveToPreferences(getPreferenceStore());
			}
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
		return ""; //$NON-NLS-1$
	}
}
