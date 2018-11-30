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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * XML symbol provider.
 *
 */
class XMLSymbolsProvider {

	private static final Logger LOGGER = Logger.getLogger(XMLSymbolsProvider.class.getName());
	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLSymbolsProvider(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<SymbolInformation> findDocumentSymbols(DOMDocument xmlDocument) {
		List<SymbolInformation> symbols = new ArrayList<>();
		xmlDocument.getRoots().forEach(node -> {
			try {
				provideFileSymbolsInternal(node, "", symbols);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "XMLSymbolsProvider was given a BadLocation by a 'node' variable", e);
			}
		});
		return symbols;
	}

	private void provideFileSymbolsInternal(DOMNode node, String container, List<SymbolInformation> symbols)
			throws BadLocationException {
		if (!isNodeSymbol(node)) {
			return;
		}
		String name = nodeToName(node);
		DOMDocument xmlDocument = node.getOwnerDocument();
		Position start = xmlDocument.positionAt(node.getStart());
		Position end = xmlDocument.positionAt(node.getEnd());
		Range range = new Range(start, end);
		Location location = new Location(xmlDocument.getDocumentURI(), range);
		SymbolInformation symbol = new SymbolInformation(name, getSymbolKind(node), location, container);

		symbols.add(symbol);

		node.getChildren().forEach(child -> {
			try {
				provideFileSymbolsInternal(child, name, symbols);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "XMLSymbolsProvider was given a BadLocation by the provided 'node' variable",
						e);
			}
		});

	}

	private SymbolKind getSymbolKind(DOMNode node) {
		if (node.isProcessingInstruction() || node.isProlog()) {
			return SymbolKind.Property;
		} else if (node.isDoctype()) {
			return SymbolKind.Enum;
		} else if (node.isDTDElementDecl() || node.isDTDAttListDecl() || node.isDTDEntityDecl()) {
			return SymbolKind.EnumMember;
		}
		return SymbolKind.Field;
	}

	private boolean isNodeSymbol(DOMNode node) {
		return node.isElement() || node.isDoctype() || node.isProcessingInstruction() || node.isProlog()
				|| node.isDTDElementDecl() || node.isDTDAttListDecl() || node.isDTDEntityDecl();
	}

	private static String nodeToName(DOMNode node) {
		String name = null;
		if (node.isElement()) {
			name = ((Element) node).getTagName();
		} else if (node.isProcessingInstruction() || node.isProlog()) {
			name = ((ProcessingInstruction) node).getTarget();
		} else if (node.isDoctype()) {
			name = "DOCTYPE:" + ((DocumentType) node).getName();
		} else if (node.isDTDElementDecl()) {
			name = ((DTDElementDecl) node).getName();
		} else if (node.isDTDAttListDecl()) {
			name = "AttList (TODO)";
		} else if (node.isDTDEntityDecl()) {
			name = node.getNodeName();
		}

		if (node.hasAttributes()) {
			String id = node.getAttribute("id");
			String classes = node.getAttribute("class");

//			if (id) {
//				name += `#${id.replace(/[\"\']/g, '')}`;
//			}
//
//			if (classes) {
//				name += classes.replace(/[\"\']/g, '').split(/\s+/).map(className => `.${className}`).join('');
//			}
		}

		return name != null ? name : "?";
	}
}
