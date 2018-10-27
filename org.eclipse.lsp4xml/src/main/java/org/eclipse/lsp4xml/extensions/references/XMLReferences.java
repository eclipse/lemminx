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

/**
 * XML reference list for a given document.
 *
 */
public class XMLReferences {

	private final Predicate<XMLDocument> documentPredicate;

	private final List<XMLReference> references;

	XMLReferences(Predicate<XMLDocument> documentPredicate) {
		this.documentPredicate = documentPredicate;
		this.references = new ArrayList<>();
	}

	public XMLReference from(String from) {
		XMLReference reference = new XMLReference(from);
		references.add(reference);
		return reference;
	}

	boolean canApply(XMLDocument document) {
		return documentPredicate.test(document);
	}

	void collectNodes(Node node, Consumer<Node> collector) throws XPathExpressionException {
		for (XMLReference reference : references) {
			if (reference.match(node)) {
				reference.collect(node, collector);
			}
		}
	}

}
