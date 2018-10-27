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
package org.eclipse.lsp4xml.extensions.references;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.dom.XMLDocument;

public class XMLReferencesManager {

	private static final XMLReferencesManager INSTANCE = new XMLReferencesManager();

	public static XMLReferencesManager getInstance() {
		return INSTANCE;
	}

	class XMLReferencesInfo {

		private final XMLReference references;
		private final Predicate<XMLDocument> documentPredicate;

		public XMLReferencesInfo(XMLReference references, Predicate<XMLDocument> documentPredicate) {
			this.references = references;
			this.documentPredicate = documentPredicate;
		}

		public boolean canApply(XMLDocument document) {
			return documentPredicate.test(document);
		}

		public boolean matchReference(Node node) {
			return references.match(node);
		}

		public void collectNodes(Node node, Consumer<Node> collector) throws XPathExpressionException {
			references.collect(node, collector);
		}
	}

	private final List<XMLReferencesInfo> referenceInfos;

	public XMLReferencesManager() {
		this.referenceInfos = new ArrayList<>();
	}

	public XMLReference addReference(String from, Predicate<XMLDocument> documentPredicate) {
		XMLReference reference = new XMLReference(from);
		registerReference(reference, documentPredicate);
		return reference;
	}
	private void registerReference(XMLReference references, Predicate<XMLDocument> documentPredicate) {
		referenceInfos.add(new XMLReferencesInfo(references, documentPredicate));
	}

	public void collect(Node node, Consumer<Node> collector) {
		XMLDocument document = node.getOwnerDocument();
		for (XMLReferencesInfo info : referenceInfos) {
			if (info.canApply(document)) {
				if (info.matchReference(node)) {
					try {
						info.collectNodes(node, collector);
					} catch (XPathExpressionException e) {
						// TODO!!!
						e.printStackTrace();
					}
				}
			}
		}
	}

}
