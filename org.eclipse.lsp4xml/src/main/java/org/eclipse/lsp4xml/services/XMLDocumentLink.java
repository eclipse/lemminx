/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * XML document link support.
 *
 */
class XMLDocumentLink {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLDocumentLink(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<DocumentLink> findDocumentLinks(XMLDocument document) {
		List<DocumentLink> newLinks = new ArrayList<>();
		for (IDocumentLinkParticipant participant : extensionsRegistry.getDocumentLinkParticipants()) {
			participant.findDocumentLinks(document, newLinks);
		}
		// TODO: call extension
		/*
		 * let rootAbsoluteUrl: Uri | null = null;
		 * 
		 * Scanner scanner = XMLScanner.createScanner(document.getText(), 0); TokenType
		 * token = scanner.scan(); let afterHrefOrSrc = false; let afterBase = false;
		 * let base: string | undefined = void 0; while (token != TokenType.EOS) {
		 * switch (token) { case TokenType.StartTag: if (!base) { let tagName =
		 * scanner.getTokenText().toLowerCase(); afterBase = tagName === 'base'; }
		 * break; case TokenType.AttributeName: let attributeName =
		 * scanner.getTokenText().toLowerCase(); afterHrefOrSrc = attributeName ===
		 * 'src' || attributeName === 'href'; break; case TokenType.AttributeValue: if
		 * (afterHrefOrSrc) { let attributeValue = scanner.getTokenText(); if
		 * (!afterBase) { // don't highlight the base link itself let link =
		 * createLink(document, documentContext, attributeValue,
		 * scanner.getTokenOffset(), scanner.getTokenEnd(), base); if (link) {
		 * newLinks.push(link); } } if (afterBase && typeof base === 'undefined') { base
		 * = normalizeRef(attributeValue, document.languageId); if (base &&
		 * documentContext) { base = documentContext.resolveReference(base,
		 * document.uri); } } afterBase = false; afterHrefOrSrc = false; } break; }
		 * token = scanner.scan(); }
		 */
		return newLinks;
	}
}
