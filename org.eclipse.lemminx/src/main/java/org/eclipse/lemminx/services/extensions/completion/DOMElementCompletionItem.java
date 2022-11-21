/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.completion;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.SnippetsBuilder;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.utils.XMLBuilder;
import org.eclipse.lsp4j.MarkupContent;

/**
 * Element completion item created from an existing element.
 *
 */
public class DOMElementCompletionItem extends AbstractElementCompletionItem<DOMElement, Void> {

	public DOMElementCompletionItem(DOMElement element, ICompletionRequest request) {
		super(element.getTagName(), element, null, request);
	}

	@Override
	protected String generateFullElementContent(boolean generateEndTag) {
		DOMElement element = getSourceElement();
		ICompletionRequest request = getRequest();
		String tag = element.getTagName();
		XMLBuilder xml = new XMLBuilder(getRequest().getSharedSettings(), "",
				getLineDelimiter(request));
		xml.startElement(tag, false);
		if (generateEndTag) {
			if (element.isSelfClosed()) {
				xml.selfCloseElement();
			} else {
				xml.closeStartElement();
				if (request.isCompletionSnippetsSupported()) {
					xml.append(SnippetsBuilder.tabstops(1));
				}
				xml.endElement(tag);
			}
		} else {
			if (!hasContentAfterTagName(element)) {
				// In case of
				// <foo>
				// <foo attr=''
				// we must not close the element with '>' after foo, otherwise XML is broken
				// only <foo is allowed to close with '>'
				xml.closeStartElement();
			}
		}
		if (request.isCompletionSnippetsSupported()) {
			xml.append(SnippetsBuilder.tabstops(0));
		}
		return xml.toString();
	}

	private static String getLineDelimiter(ICompletionRequest request) {
		try {
			return request.getXMLDocument().getTextDocument().lineDelimiter(0);
		} catch (BadLocationException e) {
			return System.lineSeparator();
		}
	}

	@Override
	protected MarkupContent generateDocumentation() {
		return null;
	}

	@Override
	protected String getResolverParticipantId() {
		return ElementEndTagCompletionResolver.PARTICIPANT_ID;
	}
}
