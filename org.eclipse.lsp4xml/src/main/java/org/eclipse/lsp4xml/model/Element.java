package org.eclipse.lsp4xml.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
		// Try to get xmlns attribute in the element
		String rootElementNamespaceDeclarationName = (prefix != null && prefix.length() > 0) ? "xmlns:" + prefix //$NON-NLS-1$
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
				String namespaceURI = ((Element) parent).getNamespaceURI();
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
