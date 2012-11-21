package com.googlecode.cppcheclipse.core;

import java.io.File;
import java.util.Collection;


public interface IToolchainSettings {

	public Collection<File> getUserIncludes();

	public Collection<File> getSystemIncludes();

	public Collection<Symbol> getUserSymbols();

	public Collection<Symbol> getSystemSymbols();
}