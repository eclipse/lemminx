/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.services.extensions.codelens;

import org.eclipse.lsp4xml.dom.DOMDocument;

/**
 * CodeLens request API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ICodeLensRequest {

	/**
	 * Returns the DOM document.
	 * 
	 * @return the DOM document
	 */
	DOMDocument getDocument();

	/**
	 * Returns true if the given code lens kind is supported by the client and false
	 * otherwise.
	 * 
	 * @param kind code lens kind
	 * @return true if the given code lens kind is supported by the client and false
	 *         otherwise.
	 */
	boolean isSupportedByClient(String kind);
}
