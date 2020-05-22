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

import org.eclipse.lemminx.services.extensions.ISharedSettingsRequest;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

/**
 * Factory to create LSP4J {@link MarkupContent}
 * 
 * @author Angelo ZERR
 *
 */
public class MarkupContentFactory {

	/**
	 * Create the markup content according the given markup kind and the capability
	 * of the client.
	 * 
	 * @param value         the documentation value
	 * @param preferredKind the preferred markup kind
	 * @return the markup content according the given markup kind and the capability
	 *         of the client.
	 */
	public static MarkupContent createMarkupContent(String value, String preferredKind, ISharedSettingsRequest support) {
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
	 * @param values            the list of documentation values
	 * @param markupKindSupport the markup kind support
	 * @return the markup content according the given markup kind and the capability
	 *         of the client.
	 */
	public static MarkupContent creatMarkupContent(List<String> values, ISharedSettingsRequest markupKindSupport) {
		String kind = getKind(markupKindSupport);
		if (values.size() == 1) {
			return new MarkupContent(kind, values.get(0));
		}
		String retValue = aggregateContent(values, kind);
		return new MarkupContent(kind, retValue);
	}

	/**
	 * Returns the result of values aggregation according the given markup kind
	 * support.
	 * 
	 * @param values            the list of documentation values
	 * @param markupKindSupport the markup kind support
	 * @return the result of values aggregation according the given markup kind
	 *         support.
	 */
	public static String aggregateContent(List<String> values, ISharedSettingsRequest markupKindSupport) {
		if (values.size() == 1) {
			return values.get(0);
		}
		String kind = getKind(markupKindSupport);
		return aggregateContent(values, kind);
	}

	private static String getKind(ISharedSettingsRequest request) {
		return request.canSupportMarkupKind(MarkupKind.MARKDOWN) ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT;
	}

	private static String aggregateContent(List<String> values, String kind) {
		StringBuilder content = new StringBuilder();
		for (String value : values) {
			if (content.length() > 0) {
				if (kind.equals(MarkupKind.MARKDOWN)) {
					content.append(System.lineSeparator());
					content.append(System.lineSeparator());
					content.append("___");
				}
				content.append(System.lineSeparator());
				content.append(System.lineSeparator());
			}
			content.append(value);
		}
		return content.toString();
	}
}
