/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.eclipse.lsp4xml.dom.DTDNotationDecl;
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

	public List<SymbolInformation> findSymbolInformations(DOMDocument xmlDocument, CancelChecker cancelChecker) {
		AtomicLong count = new AtomicLong();
		List<SymbolInformation> symbols = new ArrayList<>();
		boolean isDTD = xmlDocument.isDTD();
		for (DOMNode node : xmlDocument.getRoots()) {
			try {
				findSymbolInformations(node, "", symbols, (node.isDoctype() && isDTD), count, cancelChecker);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"XMLSymbolsProvider#findSymbolInformations was given a BadLocation by a 'node' variable", e);
			}
		}
		/* Uncomment that to avoid returning big symbol provider
		 if (count.longValue() > 100) {
			throw new CancellationException("too long");
		}*/
		return symbols;
	}

	public List<DocumentSymbol> findDocumentSymbols(DOMDocument xmlDocument, CancelChecker cancelChecker) {
		AtomicLong count = new AtomicLong();
		List<DocumentSymbol> symbols = new ArrayList<>();
		boolean isDTD = xmlDocument.isDTD();
		List<DOMNode> nodesToIgnore = new ArrayList<>();
		xmlDocument.getRoots().forEach(node -> {
			try {
				if ((node.isDoctype() && isDTD)) {
					nodesToIgnore.add(node);
				}
				findDocumentSymbols(node, symbols, nodesToIgnore, count, cancelChecker);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE,
						"XMLSymbolsProvider#findDocumentSymbols was given a BadLocation by a 'node' variable", e);
			}
		});
		/* Uncomment that to avoid returning big symbol provider
		 if (count.longValue() > 100) {
			throw new CancellationException("too long");
		}*/
		return symbols;
	}

	private void findDocumentSymbols(DOMNode node, List<DocumentSymbol> symbols, List<DOMNode> nodesToIgnore,
			AtomicLong count, CancelChecker cancelChecker) throws BadLocationException {
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
			symbols.add(symbol);
			count.incrementAndGet();

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
					findDocumentSymbols(attrDecl, childrenSymbols, null, count, cancelChecker);
					if (attrDecl instanceof DTDAttlistDecl) {
						DTDAttlistDecl decl = (DTDAttlistDecl) attrDecl;
						List<DTDAttlistDecl> otherAttributeDecls = decl.getInternalChildren();
						if (otherAttributeDecls != null) {
							for (DTDAttlistDecl internalDecl : otherAttributeDecls) {
								findDocumentSymbols(internalDecl, childrenSymbols, null, count, cancelChecker);
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
				findDocumentSymbols(child, childrenOfChild, nodesToIgnore, count, cancelChecker);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, "XMLSymbolsProvider was given a BadLocation by the provided 'node' variable",
						e);
			}
		});
	}

	private void findSymbolInformations(DOMNode node, String container, List<SymbolInformation> symbols,
			boolean ignoreNode, AtomicLong count, CancelChecker cancelChecker) throws BadLocationException {
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
			count.incrementAndGet();
			symbols.add(symbol);
		}
		final String containerName = name;
		node.getChildren().forEach(child -> {
			try {
				findSymbolInformations(child, containerName, symbols, false, count, cancelChecker);
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
