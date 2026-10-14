/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.semantictokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SemanticTokens;

/**
 * Builder for LSP semantic tokens encoded data.
 *
 * <p>
 * Tokens are collected by offset and sorted before encoding. This allows
 * multiple participants to add tokens in any order.
 * </p>
 *
 * <p>
 * Each token is encoded as 5 integers: deltaLine, deltaStartChar, length,
 * tokenType, tokenModifiers.
 * </p>
 */
public class SemanticTokensData {

	private final DOMDocument document;
	private final List<RawToken> tokens;

	public SemanticTokensData(DOMDocument document) {
		this.document = document;
		this.tokens = new ArrayList<>();
	}

	/**
	 * Add a semantic token at the given offset with the given length, token type
	 * index, and token modifiers bitmask.
	 *
	 * @param offset         the offset in the document.
	 * @param length         the length of the token.
	 * @param tokenType      the token type index (from the legend).
	 * @param tokenModifiers the token modifiers bitmask.
	 */
	public void addToken(int offset, int length, int tokenType, int tokenModifiers) {
		tokens.add(new RawToken(offset, length, tokenType, tokenModifiers));
	}

	/**
	 * Build the {@link SemanticTokens} result.
	 *
	 * @return the semantic tokens result.
	 */
	public SemanticTokens build() {
		Collections.sort(tokens);
		List<Integer> data = new ArrayList<>(tokens.size() * 5);
		int previousLine = 0;
		int previousStartChar = 0;
		for (RawToken token : tokens) {
			try {
				Position position = document.positionAt(token.offset);
				int line = position.getLine();
				int startChar = position.getCharacter();

				int deltaLine = line - previousLine;
				int deltaStartChar = deltaLine == 0 ? startChar - previousStartChar : startChar;

				data.add(deltaLine);
				data.add(deltaStartChar);
				data.add(token.length);
				data.add(token.tokenType);
				data.add(token.tokenModifiers);

				previousLine = line;
				previousStartChar = startChar;
			} catch (BadLocationException e) {
				// ignore
			}
		}
		return new SemanticTokens(data);
	}

	/**
	 * Returns true if no tokens have been added.
	 *
	 * @return true if no tokens have been added.
	 */
	public boolean isEmpty() {
		return tokens.isEmpty();
	}

	private static class RawToken implements Comparable<RawToken> {
		final int offset;
		final int length;
		final int tokenType;
		final int tokenModifiers;

		RawToken(int offset, int length, int tokenType, int tokenModifiers) {
			this.offset = offset;
			this.length = length;
			this.tokenType = tokenType;
			this.tokenModifiers = tokenModifiers;
		}

		@Override
		public int compareTo(RawToken other) {
			return Integer.compare(this.offset, other.offset);
		}
	}
}
