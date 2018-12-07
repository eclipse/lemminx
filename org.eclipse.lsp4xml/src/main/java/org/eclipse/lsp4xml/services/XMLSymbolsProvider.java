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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
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

	public List<SymbolInformation> findSymbolInformations(DOMDocument xmlDocument) {
		List<SymbolInformation> symbols = new ArrayList<>();
		boolean isDTD = xmlDocument.isDTD();
		xmlDocument.getRoots().forEach(node -> {
			try {
				findSymbolInformations(node, "", symbols, (node.isDoctype() && isDTD));
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"XMLSymbolsProvider#findSymbolInformations was given a BadLocation by a 'node' variable", e);
			}
		});
		return symbols;
	}

	public List<DocumentSymbol> findDocumentSymbols(DOMDocument xmlDocument) {
		List<DocumentSymbol> symbols = new ArrayList<>();
		boolean isDTD = xmlDocument.isDTD();
		xmlDocument.getRoots().forEach(node -> {
			try {
				findDocumentSymbols(node, symbols, (node.isDoctype() && isDTD));
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"XMLSymbolsProvider#findDocumentSymbols was given a BadLocation by a 'node' variable", e);
			}
		});
		return symbols;
	}

	private void findDocumentSymbols(DOMNode node, List<DocumentSymbol> symbols, boolean ignoreNode)
			throws BadLocationException {
		if (!isNodeSymbol(node)) {
			return;
		}
		boolean hasChildNodes = node.hasChildNodes();
		List<DocumentSymbol> children = symbols;
		if (!ignoreNode) {
			String name = nodeToName(node);
			DOMDocument xmlDocument = node.getOwnerDocument();
			Range selectionRange = getSymbolRange(node);
			Range range = selectionRange; // getSymbolRange((node.getOwnerDocument() != null ? node.getOwnerDocument() : xmlDocument));
			children = hasChildNodes ? new ArrayList<>() : Collections.emptyList();
			DocumentSymbol symbol = new DocumentSymbol(name, getSymbolKind(node), range, selectionRange, null,
					children);
			symbols.add(symbol);
		}
		if (!hasChildNodes) {
			return;
		}
		final List<DocumentSymbol> childrenOfChild = children;
		node.getChildren().forEach(child -> {
			try {
				findDocumentSymbols(child, childrenOfChild, false);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "XMLSymbolsProvider was given a BadLocation by the provided 'node' variable",
						e);
			}
		});
	}

	private void findSymbolInformations(DOMNode node, String container, List<SymbolInformation> symbols,
			boolean ignoreNode) throws BadLocationException {
		if (!isNodeSymbol(node)) {
			return;
		}
		String name = "";
		if (!ignoreNode) {
			name = nodeToName(node);
			DOMDocument xmlDocument = node.getOwnerDocument();
			Range range = getSymbolRange(node);
			Location location = new Location(xmlDocument.getDocumentURI(), range);
			SymbolInformation symbol = new SymbolInformation(name, getSymbolKind(node), location, container);
			symbols.add(symbol);
		}
		final String containerName = name;
		node.getChildren().forEach(child -> {
			try {
				findSymbolInformations(child, containerName, symbols, false);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "XMLSymbolsProvider was given a BadLocation by the provided 'node' variable",
						e);
			}
		});
	}

	private static Range getSymbolRange(DOMNode node) throws BadLocationException {
		DOMDocument xmlDocument = node.getOwnerDocument();
		Position start = xmlDocument.positionAt(node.getStart());
		Position end = xmlDocument.positionAt(node.getEnd());
		return new Range(start, end);
	}

	private static SymbolKind getSymbolKind(DOMNode node) {
		if (node.isProcessingInstruction() || node.isProlog()) {
			return SymbolKind.Property;
		} else if (node.isDoctype()) {
			return SymbolKind.Struct;
		} else if (node.isDTDElementDecl() || node.isDTDEntityDecl()) {
			return SymbolKind.Property;
		} else if (node.isDTDAttListDecl()) {
			return SymbolKind.Key;
		}
		return SymbolKind.Field;
	}

	private static boolean isNodeSymbol(DOMNode node) {
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
			DTDAttlistDecl attr = (DTDAttlistDecl) node;
			name = attr.getElementName() + "/@" + attr.getName();
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
