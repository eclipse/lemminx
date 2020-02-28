/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.utils;

import java.util.List;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

/**
 * Factory to create LSP4J {@link MarkupContent}
 * 
 * @author Angelo ZERR
 *
 */
public class MarkupContentFactory {

	public static interface IMarkupKindSupport {

		/**
		 * Returns <code>true</code> if the client can support the given Markup kind for
		 * documentation and <code>false</code> otherwise.
		 * 
		 * @param kind the markup kind
		 * @return <code>true</code> if the client can support the given Markup kind for
		 *         documentation and <code>false</code> otherwise.
		 */
		boolean canSupportMarkupKind(String kind);
	}

	/**
	 * Create the markup content according the given markup kind and the capability
	 * of the client.
	 * 
	 * @param value         the documentation value
	 * @param preferredKind the preferred markup kind
	 * @return the markup content according the given markup kind and the capability
	 *         of the client.
	 */
	public static MarkupContent createMarkupContent(String value, String preferredKind, IMarkupKindSupport support) {
		if (value == null) {
			return null;
		}
		MarkupContent content = new MarkupContent();
		if (MarkupKind.MARKDOWN.equals(preferredKind) && support.canSupportMarkupKind(preferredKind)) {
			String markdown = MarkdownConverter.convert(value);
			content.setValue(markdown);
			content.setKind(MarkupKind.MARKDOWN);
		} else {
			content.setValue(value);
			content.setKind(MarkupKind.PLAINTEXT);
		}
		return content;
	}

	/**
	 * Create the markup content according the given markup kind and the capability
	 * of the client.
	 * 
	 * @param values         the list of documentation values
	 * @return the markup content according the given markup kind and the capability
	 *         of the client.
	 */
	public static MarkupContent creatMarkupContent(List<String> values, IMarkupKindSupport request) {
		String kind = request.canSupportMarkupKind(MarkupKind.MARKDOWN) ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT;
		if (values.size() ==1) {
			return new MarkupContent(kind, values.get(0));
		}
		StringBuilder retValue = new StringBuilder();
		for (String value : values) {
			retValue.append(value);
			if (kind.equals(MarkupKind.MARKDOWN)) {
				retValue.append("___");
			}
			retValue.append(System.lineSeparator() );
			retValue.append(System.lineSeparator() );
		}
		return new MarkupContent(kind, retValue.toString());
	}
}
