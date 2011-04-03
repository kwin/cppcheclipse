package com.googlecode.cppcheclipse.core;

import java.io.IOException;
import java.util.StringTokenizer;

public class Symbol {

	private final static String DELIMITER = "=";

	private String name;
	private String value;
	private final boolean isCDTDefined;
	private final boolean isSystemDefined;

	public Symbol(String name, String value, boolean isCDTDefined,
			boolean isSystemDefined) {
		super();
		this.name = name;
		this.value = value;
		this.isCDTDefined = isCDTDefined;
		this.isSystemDefined = isSystemDefined;
	}

	public Symbol(String name, String value) {
		this(name, value, false, false);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * returns the symbol in the form "-D<name>{=<value}"
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("-D");
		result.append(name);
		if (value != null && value.length() > 0) {
			result.append("=").append(value);
		}
		return result.toString();
	}

	public boolean isCDTDefined() {
		return isCDTDefined;
	}

	public boolean isSystemDefined() {
		return isSystemDefined;
	}

	public String serialize() throws IOException {
		// only serialize non system defined symbols
		if (isCDTDefined) {
			throw new IOException("CDT-defined symbols can't be serialized");
		}

		StringBuffer serialization = new StringBuffer();
		// serialize in a platform-portable way
		serialization.append(name).append(DELIMITER).append(value);
		return serialization.toString();
	}

	public static Symbol deserialize(String serialization) throws IOException,
			ClassNotFoundException {
		StringTokenizer tokenizer = new StringTokenizer(serialization,
				DELIMITER);
		String name = tokenizer.nextToken();
		String value = tokenizer.nextToken();
		return new Symbol(name, value);
	}
}
