/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * Completion request API.
 *
 */
public interface ICompletionRequest extends IPositionRequest {

	Range getReplaceRange();

	XMLFormattingOptions getFormattingSettings();

	CompletionSettings getCompletionSettings();

	XMLGenerator getXMLGenerator() throws BadLocationException;

	String getFilterForStartTagName(String tagName);

	String getInsertAttrValue(String value);

	/**
	 * Returns <code>true</code> if client can support the given Markup kind for
	 * documentation and <code>false</code> otherwise.
	 * 
	 * @param kind the markup kind
	 * @return <code>true</code> if client can support the given Markup kind for
	 *         documentation and <code>false</code> otherwise.
	 */
	boolean canSupportMarkupKind(String kind);

	/**
	 * Create the markup content according the given markup kind and the capability
	 * of the client.
	 * 
	 * @param value the documentation value
	 * @param kind  the {@link MarkupKind}
	 * @return the markup content according the given markup kind and the capability
	 *         of the client.
	 */
	MarkupContent createMarkupContent(String value, String kind);
}
