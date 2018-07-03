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
package org.eclipse.xml.languageserver.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;
import org.eclipse.xml.languageserver.model.Node;
import org.eclipse.xml.languageserver.model.XMLDocument;

/**
 * XML symbol provider.
 *
 */
class XMLSymbolsProvider {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLSymbolsProvider(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<SymbolInformation> findDocumentSymbols(XMLDocument xmlDocument) {
		List<SymbolInformation> symbols = new ArrayList<>();
		xmlDocument.getRoots().forEach(node -> {
			try {
				provideFileSymbolsInternal(node, "", symbols);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		});
		return symbols;
	}

	private void provideFileSymbolsInternal(Node node, String container, List<SymbolInformation> symbols)
			throws BadLocationException {
		String name = nodeToName(node);
		XMLDocument xmlDocument = node.getOwnerDocument();
		Position start = xmlDocument.positionAt(node.start);
		Position end = xmlDocument.positionAt(node.end);
		Range range = new Range(start, end);
		Location location = new Location(xmlDocument.getUri(), range);
		SymbolInformation symbol = new SymbolInformation(name, SymbolKind.Field, location, container);

		symbols.add(symbol);

		node.children.forEach(child -> {
			try {
				provideFileSymbolsInternal(child, name, symbols);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}

	private static String nodeToName(Node node) {
		String name = node.tag;

		if (node.attributes != null) {
			String id = node.attributes.get("id");
			String classes = node.attributes.get("class");

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
