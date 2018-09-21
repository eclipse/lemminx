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
package org.eclipse.lsp4xml.commons;

import org.eclipse.lsp4j.TextDocumentItem;

/**
 * {@link TextDocument} factory.
 *
 */
public interface ITextDocumentFactory {

	/**
	 * Create a {@link TextDocument} instance from the given
	 * {@link TextDocumentItem}.
	 * 
	 * @param document
	 * @return a {@link TextDocument} instance from the given
	 *         {@link TextDocumentItem}.
	 */
	TextDocument createDocument(TextDocumentItem document);
}
