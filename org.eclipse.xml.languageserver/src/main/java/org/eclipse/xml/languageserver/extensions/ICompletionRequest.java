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
package org.eclipse.xml.languageserver.extensions;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xml.languageserver.model.XMLDocument;

/**
 * Completion request API.
 *
 */
public interface ICompletionRequest {

	/**
	 * Returns the offset where completion was trigerred.
	 * 
	 * @return
	 */
	int getOffset();

	/**
	 * Returns the LSP text document.
	 * 
	 * @return the LSP text document.
	 */
	TextDocumentItem getDocument();

	/**
	 * Returns the XML document.
	 * 
	 * @return the XML document.
	 */
	XMLDocument getXMLDocument();

	String getCurrentTag();

	String getCurrentAttributeName();
}
