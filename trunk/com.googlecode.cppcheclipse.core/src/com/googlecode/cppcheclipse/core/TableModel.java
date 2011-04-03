package com.googlecode.cppcheclipse.core;

import java.io.IOException;

public interface TableModel<Element> extends Iterable<Element> {
	void remove(Element element);
	void removeAll();
	Element[] toArray();
	
	void save() throws IOException;
}
