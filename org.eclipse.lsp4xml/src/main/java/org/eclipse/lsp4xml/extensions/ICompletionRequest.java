/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * Completion request API.
 *
 */
public interface ICompletionRequest {

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
	
	FormattingOptions getFormattingSettings();
}
