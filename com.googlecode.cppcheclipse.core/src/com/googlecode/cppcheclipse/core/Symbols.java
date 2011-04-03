package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 
 * @author Konrad Windszus
 * Model class for symbols (also called configurations or macros)
 */
public class Symbols implements TableModel<Symbol> {
	private Collection<Symbol> symbols; // key and value
	
	private static final String DELIMITER = "\n";
	private final IPreferenceStore projectPreferences;

	/*
	 * Creates empty list of symbols
	 */
	public Symbols() {
		symbols = new LinkedList<Symbol>();
		projectPreferences = null;
	}
	
	public Symbols(IPreferenceStore projectPreferences, IToolchainSettings toolchainSettings) {
		this.projectPreferences = projectPreferences;
		this.symbols = new LinkedList<Symbol>();
		
		// load from project preferences
		load(toolchainSettings);
	}
	
	private void load(IToolchainSettings toolchainSettings) {
		if (projectPreferences.getBoolean(IPreferenceConstants.P_INCLUDE_CDT_USER_SYMBOLS)) {
			this.symbols.addAll(toolchainSettings.getUserSymbols());
		}
		
		if (projectPreferences.getBoolean(IPreferenceConstants.P_INCLUDE_CDT_SYSTEM_SYMBOLS)) {
			this.symbols.addAll(toolchainSettings.getSystemSymbols());
		}
		String symbols = projectPreferences
				.getString(IPreferenceConstants.P_SYMBOLS);
		StringTokenizer tokenizer = new StringTokenizer(symbols, DELIMITER);
		while (tokenizer.hasMoreTokens()) {
			try {
				Symbol symbol = Symbol.deserialize(tokenizer
						.nextToken());
				addSymbol(symbol);
			} catch (Exception e) {
				CppcheclipsePlugin.logWarning("Could not load defined symbols", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.core.TableModel#save()
	 */
	public void save() throws IOException {
		StringBuffer symbolsSerialization = new StringBuffer();
		for (Symbol symbol : symbols) {
			// only serialize user-defined symbols
			if (!symbol.isCDTDefined())
				symbolsSerialization.append(symbol.serialize()).append(DELIMITER);
		}

		projectPreferences.setValue(IPreferenceConstants.P_SYMBOLS,
				symbolsSerialization.toString());

		if (projectPreferences instanceof IPersistentPreferenceStore) {
			((IPersistentPreferenceStore) projectPreferences).save();
		}
	}

	public void addSymbol(Symbol symbol) {
		symbols.add(symbol);
	}
	
	public void addSymbols(Collection<Symbol> symbols) {
		this.symbols.addAll(symbols);
	}
	
	public void remove(Symbol symbol) {
		symbols.remove(symbol);
	}

	public void removeAll() {
		symbols.clear();
	}
	
	public void removeCDTSystemSymbols() {
		Iterator<Symbol> iterator = symbols.iterator();
		while (iterator.hasNext()) {
			Symbol symbol = iterator.next();
			if (symbol.isCDTDefined() && symbol.isSystemDefined()) {
				iterator.remove();
			}
		}
	}
	
	public void removeUserSymbols() {
		Iterator<Symbol> iterator = symbols.iterator();
		while (iterator.hasNext()) {
			Symbol symbol = iterator.next();
			if (!symbol.isCDTDefined()) {
				iterator.remove();
			}
		}
	}
	
	public void removeCDTUserSymbols() {
		Iterator<Symbol> iterator = symbols.iterator();
		while (iterator.hasNext()) {
			Symbol symbol = iterator.next();
			if (symbol.isCDTDefined() && !symbol.isSystemDefined()) {
				iterator.remove();
			}
		}
	}

	public Iterator<Symbol> iterator() {
		return symbols.iterator();
	}

	public Symbol[] toArray() {
		return symbols.toArray(new Symbol[0]);
	}
}
