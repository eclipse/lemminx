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
package org.eclipse.lsp4xml.dom.parser;

/**
 * Scanner API.
 *
 */
public interface Scanner {

	TokenType scan();

	TokenType getTokenType();

	/**
	 * Starting offset position of the current token
	 * @return int of token's start offset
	 */
	int getTokenOffset();

	int getTokenLength();

	/**
	 * Ending offset position of the current token
	 * @return int of token's end offset
	 */
	int getTokenEnd();

	String getTokenText();

	String getTokenError();

	ScannerState getScannerState();
}
