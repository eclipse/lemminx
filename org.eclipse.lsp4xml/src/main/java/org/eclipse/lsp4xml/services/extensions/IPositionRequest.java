package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;

public interface IPositionRequest {

	/**
	 * Returns the offset where completion was triggered.
	 * 
	 * @return the offset where completion was triggered
	 */
	int getOffset();

	/**
	 * Returns the position
	 * 
	 * @return the position
	 */
	Position getPosition();

	/**
	 * Returns the node where completion was triggered.
	 * 
	 * @return the offset where completion was triggered
	 */
	Node getNode();

	Node getParentNode();

	/**
	 * Returns the XML document.
	 * 
	 * @return the XML document.
	 */
	XMLDocument getXMLDocument();

	String getCurrentTag();

	String getCurrentAttributeName();
}
