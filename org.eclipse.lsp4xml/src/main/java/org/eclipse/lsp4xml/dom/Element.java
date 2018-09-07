/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An element node.
 * 
 * @author azerr
 *
 */
public class Element extends Node {

	public Element(int start, int end, List<Node> children, Node parent, XMLDocument ownerDocument) {
		super(start, end, children, parent, ownerDocument);
	}

	@Override
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	public String getTagName() {
		return tag;
	}

	public String getLocalName() {
		String name = getTagName();
		if (name == null) {
			return null;
		}
		int index = name.indexOf(":"); //$NON-NLS-1$
		if (index != -1) {
			name = name.substring(index + 1);
		}
		return name;
	}

	public String getPrefix() {
		String name = getTagName();
		if (name == null) {
			return null;
		}
		String prefix = null;
		int index = name.indexOf(":"); //$NON-NLS-1$
		if (index != -1) {
			prefix = name.substring(0, index);
		}
		return prefix;
	}

	public String getNamespaceURI() {
		String prefix = getPrefix();
		boolean hasPrefix = prefix != null && prefix.length() > 0;
		// Try to get xmlns attribute in the element
		String rootElementNamespaceDeclarationName = (hasPrefix) ? "xmlns:" + prefix //$NON-NLS-1$
				: "xmlns"; //$NON-NLS-1$
		String rootElementNamespace = rootElementNamespaceDeclarationName != null
				? this.getAttributeValue(rootElementNamespaceDeclarationName)
				: null;
		if (rootElementNamespace != null) {
			return rootElementNamespace;
		}
		// try to get the namespace in the parent element
		Node parent = getParent();
		while (parent != null) {
			if (parent.getNodeType() == Node.ELEMENT_NODE) {
				Element parentElement = ((Element) parent);
				String namespaceURI = hasPrefix ? parentElement.getAttributeValue("xmlns:" + prefix)
						: parentElement.getNamespaceURI();
				if (namespaceURI != null) {
					return namespaceURI;
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	public Collection<String> getAllPrefixes() {
		if (hasAttributes()) {
			Collection<String> prefixes = new ArrayList<>();
			for (String attributeName : attributeNames()) {
				if (attributeName.startsWith("xmlns:")) {
					prefixes.add(attributeName.substring("xmlns:".length(), attributeName.length()));
				}
			}
			return prefixes;
		}
		return Collections.emptyList();
	}

	public String getNamespaceURI(String prefix) {
		if (prefix == null || prefix.isEmpty()) {
			return getNamespaceURI();
		}
		return getAttributeValue("xmlns:" + prefix);
	}

}
