package org.eclipse.xml.languageserver.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.xml.languageserver.internal.parser.BadLocationException;
import org.eclipse.xml.languageserver.model.XMLDocument;
import org.eclipse.xml.languageserver.model.Node;

public class XMLSymbolsProvider {

	public static List<SymbolInformation> findDocumentSymbols(TextDocumentItem document, XMLDocument fmDocument) {
		List<SymbolInformation> symbols = new ArrayList<>();
		fmDocument.getRoots().forEach(node -> {
			try {
				provideFileSymbolsInternal(document, fmDocument, node, "", symbols);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return symbols;
	}

	private static void provideFileSymbolsInternal(TextDocumentItem document, XMLDocument fmDocument, Node node,
			String container, List<SymbolInformation> symbols) throws BadLocationException {
		String name = nodeToName(node);
		Position start = fmDocument.positionAt(node.start);
		Position end = fmDocument.positionAt(node.end);
		Range range = new Range(start, end);
		Location location = new Location(document.getUri(), range);
		SymbolInformation symbol = new SymbolInformation(name, SymbolKind.Field, location, container);

		symbols.add(symbol);

		node.children.forEach(child -> {
			try {
				provideFileSymbolsInternal(document, fmDocument, child, name, symbols);
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
