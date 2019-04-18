/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.CompletionItemKind;

/**
 * CompletionSortText class to get sortText for CompletionItem's
 */
public class CompletionSortTextHelper {

	private int i;
	private final String base;

	public CompletionSortTextHelper(CompletionItemKind kind) {
		this.base = getSortText(kind);
		i = 0;
	}

	public String next() {
		i++;
		return base + Integer.toString(i);
	}

	public static String getSortText(CompletionItemKind kind) {
		switch (kind) {
		case Variable:
		case Property: // DOMElement
		case Enum:
		case EnumMember:
		case Value:
			return "aa";
		default:
			return "zz";
		}
	}

}