/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.utils;

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
}
