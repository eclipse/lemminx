/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMText;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lemminx.dom.parser.TokenType;
import org.eclipse.lemminx.dom.parser.XMLScanner;
import org.eclipse.lemminx.services.extensions.IHoverParticipant;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.settings.XMLHoverSettings;
import org.eclipse.lemminx.utils.MarkupContentFactory;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * XML hover support.
 *
 */
class XMLHover {

	private static final Logger LOGGER = Logger.getLogger(XMLHover.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLHover(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public Hover doHover(DOMDocument xmlDocument, Position position, XMLHoverSettings settings,
			CancelChecker cancelChecker) {
		HoverRequest hoverRequest = null;
		try {
			hoverRequest = new HoverRequest(xmlDocument, position, settings, extensionsRegistry);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating HoverRequest", e);
			return null;
		}
		int offset = hoverRequest.getOffset();
		DOMNode node = hoverRequest.getNode();
		if (node == null) {
			return null;
		}
		if (node.isElement() && ((DOMElement) node).getTagName() != null) {
			// Element is hover
			DOMElement element = (DOMElement) node;
			if (element.hasEndTag() && offset >= element.getEndTagOpenOffset()) {
				Range tagRange = getTagNameRange(TokenType.EndTag, element.getEndTagOpenOffset(), offset, xmlDocument);
				if (tagRange != null) {
					return getTagHover(hoverRequest, tagRange, false);
				}
				return null;
			}

			Range tagRange = getTagNameRange(TokenType.StartTag, node.getStart(), offset, xmlDocument);
			if (tagRange != null) {
				return getTagHover(hoverRequest, tagRange, true);
			}
		} else if (node.isAttribute()) {
			DOMAttr attr = (DOMAttr) node;
			if (attr.valueContainsOffset(offset)) {
				return getAttrValueHover(hoverRequest, null);
			}
			// Attribute is hover
			return getAttrNameHover(hoverRequest, null);
		} else if (node.isText()) {
			// Text is hover
			DOMText text = (DOMText) node;
			try {
				Range textRange = new Range(xmlDocument.positionAt(text.getStart()),
						xmlDocument.positionAt(text.getEnd()));
				return getTextHover(hoverRequest, textRange);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "While creating Range in XMLHover the text's Offset was a BadLocation", e);
			}
		}
		return null;
	}

	private Hover getTextHover(HoverRequest hoverRequest, Range textRange) {
		hoverRequest.setTagRange(textRange);
		List<String> contentValues = new ArrayList<String>();
		for (IHoverParticipant participant : extensionsRegistry.getHoverParticipants()) {
			try {
				String contentValue = participant.onText(hoverRequest);
				if (contentValue != null) {
					contentValues.add(contentValue);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing IHoverParticipant#onTagText", e);
			}
		}
		return createHover(contentValues, hoverRequest);
	}

	/**
	 * Returns the LSP hover from the hovered element.
	 * 
	 * @param hoverRequest the hover request.
	 * @param tagRange     the tag range
	 * @param open         true if it's the start tag which is hovered and false if
	 *                     it's the end tag.
	 * @return the LSP hover from the hovered element.
	 */
	private Hover getTagHover(HoverRequest hoverRequest, Range tagRange, boolean open) {
		hoverRequest.setTagRange(tagRange);
		hoverRequest.setOpen(open);
		List<String> contentValues = new ArrayList<String>();
		for (IHoverParticipant participant : extensionsRegistry.getHoverParticipants()) {
			try {
				String contentValue = participant.onTag(hoverRequest);
				if (contentValue != null) {
					contentValues.add(contentValue);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing IHoverParticipant#onTag", e);
			}
		}
		return createHover(contentValues, hoverRequest);
	}

	private Range getTagNameRange(TokenType tokenType, int startOffset, int offset, DOMDocument document) {
		Scanner scanner = XMLScanner.createScanner(document.getText(), startOffset);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS
				&& (scanner.getTokenEnd() < offset || scanner.getTokenEnd() == offset && token != tokenType)) {
			token = scanner.scan();
		}
		if (token == tokenType && offset <= scanner.getTokenEnd()) {
			try {
				return new Range(document.positionAt(scanner.getTokenOffset()),
						document.positionAt(scanner.getTokenEnd()));
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "While creating Range in XMLHover the Scanner's Offset was a BadLocation", e);
				return null;
			}
		}
		return null;
	}

	/**
	 * Returns the LSP hover from the hovered attribute.
	 * 
	 * @param hoverRequest the hover request.
	 * @param attrRange    the attribute range
	 * @return the LSP hover from the hovered attribute.
	 */
	private Hover getAttrNameHover(HoverRequest hoverRequest, Range attrRange) {
		// hoverRequest.setTagRange(tagRange);
		// hoverRequest.setOpen(open);
		List<String> contentValues = new ArrayList<String>();
		for (IHoverParticipant participant : extensionsRegistry.getHoverParticipants()) {
			try {
				String contentValue = participant.onAttributeName(hoverRequest);
				if (contentValue != null) {
					contentValues.add(contentValue);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing IHoverParticipant#onTag", e);
			}
		}
		return createHover(contentValues, hoverRequest);
	}

	/**
	 * Returns the LSP hover from the hovered attribute.
	 * 
	 * @param hoverRequest the hover request.
	 * @param attrRange    the attribute range
	 * @return the LSP hover from the hovered attribute.
	 */
	private Hover getAttrValueHover(HoverRequest hoverRequest, Range attrRange) {
		// hoverRequest.setTagRange(tagRange);
		// hoverRequest.setOpen(open);
		List<String> contentValues = new ArrayList<String>();
		for (IHoverParticipant participant : extensionsRegistry.getHoverParticipants()) {
			try {
				String contentValue = participant.onAttributeValue(hoverRequest);
				if (contentValue != null) {
					contentValues.add(contentValue);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "While performing IHoverParticipant#onTag", e);
			}
		}
		return createHover(contentValues, hoverRequest);
	}

	/**
	 * Returns the aggregated LSP hover from the value list.
	 * 
	 * @param contentValues the content values.
	 * @param hoverRequest the hover request.
	 * @return the aggregated LSP hover from the value list.
	 */
	private static Hover createHover(List<String> contentValues, HoverRequest hoverRequest) {
		if (!contentValues.isEmpty()) {
			return new Hover(MarkupContentFactory.creatMarkupContent(contentValues, hoverRequest), hoverRequest.getTagRange());
		}
		return null;
	}
}
