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

/**
 * An Element node.
 *
 */
public class Element extends Node {

	String tag;
	boolean selfClosed;

	Integer startTagOpenOffset;
	Integer startTagCloseOffset; // <root|>
	// Integer startTagSelfCloseOffset;
	Integer endTagOpenOffset; // <root>|</root>
	Integer endTagCloseOffset;
	Integer endTagOffset;

	public Element(int start, int end, XMLDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	@Override
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	@Override
	public String getNodeName() {
		return getTagName();
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
			for (Attr attr : getAttributeNodes()) {
				String attributeName = attr.getName();
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

	public boolean isDocumentElement() {
		return this.equals(getOwnerDocument().getDocumentElement());
	}

	public boolean isSelfClosed() {
		return selfClosed;
	}

	public boolean isSameTag(String tagInLowerCase) {
		return this.tag != null && tagInLowerCase != null && this.tag.length() == tagInLowerCase.length()
				&& this.tag.toLowerCase().equals(tagInLowerCase);
	}

	public boolean isInStartTag(int offset) {
		if (startTagOpenOffset == null || startTagCloseOffset == null) {
			// case <|
			return true;
		}
		if (offset > startTagOpenOffset && offset <= startTagCloseOffset) {
			// case <bean | >
			return true;
		}
		return false;
	}
	
	public boolean isInEndTag(int offset) {
		if (endTagOpenOffset == null) {
			// case >|
			return false;
		}
		if (offset > endTagOpenOffset && offset <= getEnd()) {
			// case <\bean | >
			return true;
		}
		return false;
	}

	public boolean hasStartTagClose() {
		return startTagCloseOffset != null;
	}

	public Integer getStartTagOpenOffset() {
		return startTagOpenOffset;
	}

	public Integer getStartTagCloseOffset() {
		return startTagCloseOffset;
	}

	public Integer getEndTagOpenOffset() {
		return endTagOpenOffset;
	}

	/**
	 * Returns true if has a start tag.
	 * 
	 * In our source-oriented DOM, a lone end tag will cause a node to be created in
	 * the tree, unlike well-formed-only DOMs.
	 * 
	 * @return true if has a start tag.
	 */
	public boolean hasStartTag() {
		return startTagOpenOffset != null;
	}

	/**
	 * Returns true if has an end tag.
	 * 
	 * In our source-oriented DOM, sometimes Elements are "ended", even without an
	 * explicit end tag in the source.
	 * 
	 * @return true if has an end tag.
	 */
	public boolean hasEndTag() {
		return endTagOpenOffset != null;
	}


}
