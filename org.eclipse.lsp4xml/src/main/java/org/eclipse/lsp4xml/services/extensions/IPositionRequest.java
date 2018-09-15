package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.LineIndentInfo;
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

	/**
	 * Returns the parent element of the node where completion was triggered and
	 * null otherwise.
	 * 
	 * @return the parent element of the node where completion was triggered and
	 *         null otherwise.
	 */
	Element getParentElement();

	/**
	 * Returns the XML document.
	 * 
	 * @return the XML document.
	 */
	XMLDocument getXMLDocument();

	String getCurrentTag();

	String getCurrentAttributeName();

	/**
	 * Returns the line indent information of the offset where completion was
	 * triggered.
	 * 
	 * @return
	 * @throws BadLocationException
	 */
	LineIndentInfo getLineIndentInfo() throws BadLocationException;
}
