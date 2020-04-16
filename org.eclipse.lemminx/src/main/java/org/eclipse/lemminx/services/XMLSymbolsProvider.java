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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DTDAttlistDecl;
import org.eclipse.lemminx.dom.DTDDeclParameter;
import org.eclipse.lemminx.dom.DTDElementDecl;
import org.eclipse.lemminx.dom.DTDNotationDecl;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.settings.XMLSymbolSettings;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * XML symbol provider.
 *
 */
class XMLSymbolsProvider {

	private static class ResultLimitExceededException extends RuntimeException {

		private static final long serialVersionUID = 1L;

	}

	private static final Logger LOGGER = Logger.getLogger(XMLSymbolsProvider.class.getName());
	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLSymbolsProvider(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public SymbolInformationsResult findSymbolInformations(DOMDocument xmlDocument, XMLSymbolSettings symbolSettings,
			CancelChecker cancelChecker) {
		SymbolInformationsResult symbols = new SymbolInformationsResult();
		AtomicLong limit = symbolSettings.getMaxItemsComputed() > 0
				? new AtomicLong(symbolSettings.getMaxItemsComputed())
				: null;
		boolean isDTD = xmlDocument.isDTD();
		try {
			for (DOMNode node : xmlDocument.getRoots()) {
				try {
					findSymbolInformations(node, "", symbols, (node.isDoctype() && isDTD), limit, cancelChecker);
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE,
							"XMLSymbolsProvider#findSymbolInformations was given a BadLocation by a 'node' variable",
							e);
				}
			}
		} catch (ResultLimitExceededException e) {
			symbols.setResultLimitExceeded(true);
		}
		return symbols;
	}

	public DocumentSymbolsResult findDocumentSymbols(DOMDocument xmlDocument, XMLSymbolSettings symbolSettings,
			CancelChecker cancelChecker) {
		DocumentSymbolsResult symbols = new DocumentSymbolsResult();
		AtomicLong limit = symbolSettings.getMaxItemsComputed() > 0
				? new AtomicLong(symbolSettings.getMaxItemsComputed())
				: null;
		boolean isDTD = xmlDocument.isDTD();
		List<DOMNode> nodesToIgnore = new ArrayList<>();
		try {
			xmlDocument.getRoots().forEach(node -> {
				try {
					if ((node.isDoctype() && isDTD)) {
						nodesToIgnore.add(node);
					}
					findDocumentSymbols(node, symbols, limit, nodesToIgnore, cancelChecker);
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE,
							"XMLSymbolsProvider#findDocumentSymbols was given a BadLocation by a 'node' variable", e);
				}
			});
		} catch (ResultLimitExceededException e) {
			symbols.setResultLimitExceeded(true);
		}
		return symbols;
	}

	private void findDocumentSymbols(DOMNode node, List<DocumentSymbol> symbols, AtomicLong limit,
			List<DOMNode> nodesToIgnore, CancelChecker cancelChecker) throws BadLocationException {
		if (!isNodeSymbol(node)) {
			return;
		}
		cancelChecker.checkCanceled();

		boolean hasChildNodes = node.hasChildNodes();
		List<DocumentSymbol> childrenSymbols = symbols;
		if (nodesToIgnore == null || !nodesToIgnore.contains(node)) {
			String name;
			Range selectionRange;

			if (nodesToIgnore != null && node.isDTDAttListDecl()) { // attlistdecl with no elementdecl references
				DTDAttlistDecl decl = (DTDAttlistDecl) node;
				name = decl.getElementName();
				selectionRange = getSymbolRange(node, true);
			} else { // regular node
				name = nodeToName(node);
				selectionRange = getSymbolRange(node);

			}
			Range range = selectionRange;
			childrenSymbols = hasChildNodes || node.isDTDElementDecl() || node.isDTDAttListDecl() ? new ArrayList<>()
					: Collections.emptyList();
			DocumentSymbol symbol = new DocumentSymbol(name, getSymbolKind(node), range, selectionRange, null,
					childrenSymbols);
			checkLimit(limit);
			symbols.add(symbol);

			if (node.isDTDElementDecl() || (nodesToIgnore != null && node.isDTDAttListDecl())) {
				// In the case of DTD ELEMENT we try to add in the children the DTD ATTLIST
				Collection<DOMNode> attlistDecls;
				if (node.isDTDElementDecl()) {
					DTDElementDecl elementDecl = (DTDElementDecl) node;
					String elementName = elementDecl.getName();
					attlistDecls = node.getOwnerDocument().findDTDAttrList(elementName);
				} else {
					attlistDecls = new ArrayList<DOMNode>();
					attlistDecls.add(node);
				}

				for (DOMNode attrDecl : attlistDecls) {
					findDocumentSymbols(attrDecl, childrenSymbols, limit, null, cancelChecker);
					if (attrDecl instanceof DTDAttlistDecl) {
						DTDAttlistDecl decl = (DTDAttlistDecl) attrDecl;
						List<DTDAttlistDecl> otherAttributeDecls = decl.getInternalChildren();
						if (otherAttributeDecls != null) {
							for (DTDAttlistDecl internalDecl : otherAttributeDecls) {
								findDocumentSymbols(internalDecl, childrenSymbols, limit, null, cancelChecker);
							}
						}
					}
					nodesToIgnore.add(attrDecl);
				}
			}
		}
		if (!hasChildNodes) {
			return;
		}
		final List<DocumentSymbol> childrenOfChild = childrenSymbols;
		node.getChildren().forEach(child -> {
			try {
				findDocumentSymbols(child, childrenOfChild, limit, nodesToIgnore, cancelChecker);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "XMLSymbolsProvider was given a BadLocation by the provided 'node' variable",
						e);
			}
		});
	}

	private void checkLimit(AtomicLong limit) {
		if (limit == null) {
			return;
		}
		long result = limit.decrementAndGet();
		if (result < 0) {
			throw new ResultLimitExceededException();
		}
	}

	private void findSymbolInformations(DOMNode node, String container, List<SymbolInformation> symbols,
			boolean ignoreNode, AtomicLong limit, CancelChecker cancelChecker) throws BadLocationException {
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

			checkLimit(limit);
			symbols.add(symbol);
		}
		final String containerName = name;
		node.getChildren().forEach(child -> {
			try {
				findSymbolInformations(child, containerName, symbols, false, limit, cancelChecker);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "XMLSymbolsProvider was given a BadLocation by the provided 'node' variable",
						e);
			}
		});
	}

	private static Range getSymbolRange(DOMNode node) throws BadLocationException {
		return getSymbolRange(node, false);
	}

	private static Range getSymbolRange(DOMNode node, boolean useAttlistElementName) throws BadLocationException {
		Position start;
		Position end;
		DOMDocument xmlDocument = node.getOwnerDocument();

		if (node.isDTDAttListDecl() && !useAttlistElementName) {
			DTDAttlistDecl attlistDecl = (DTDAttlistDecl) node;
			DTDDeclParameter attributeNameDecl = attlistDecl.attributeName;

			if (attributeNameDecl != null) {
				start = xmlDocument.positionAt(attributeNameDecl.getStart());
				end = xmlDocument.positionAt(attributeNameDecl.getEnd());
				return new Range(start, end);
			}
		}
		start = xmlDocument.positionAt(node.getStart());
		end = xmlDocument.positionAt(node.getEnd());
		return new Range(start, end);
	}

	private static SymbolKind getSymbolKind(DOMNode node) {
		if (node.isProcessingInstruction() || node.isProlog()) {
			return SymbolKind.Property;
		} else if (node.isDoctype()) {
			return SymbolKind.Struct;
		} else if (node.isDTDElementDecl()) {
			return SymbolKind.Property;
		} else if (node.isDTDEntityDecl()) {
			return SymbolKind.Namespace;
		} else if (node.isDTDAttListDecl()) {
			return SymbolKind.Key;
		} else if (node.isDTDNotationDecl()) {
			return SymbolKind.Variable;
		}
		return SymbolKind.Field;
	}

	private static boolean isNodeSymbol(DOMNode node) {
		return node.isElement() || node.isDoctype() || node.isProcessingInstruction() || node.isProlog()
				|| node.isDTDElementDecl() || node.isDTDAttListDecl() || node.isDTDEntityDecl()
				|| node.isDTDNotationDecl();
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
			name = attr.getAttributeName();
		} else if (node.isDTDEntityDecl()) {
			name = node.getNodeName();
		} else if (node.isDTDNotationDecl()) {
			DTDNotationDecl notation = (DTDNotationDecl) node;
			name = notation.getName();
		}

		return name != null ? name : "?";
	}
}
