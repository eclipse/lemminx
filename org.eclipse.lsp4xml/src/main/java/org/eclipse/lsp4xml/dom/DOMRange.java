package org.eclipse.lsp4xml.dom;

public interface DOMRange {

	int getStart();

	int getEnd();

	DOMDocument getOwnerDocument();
}
