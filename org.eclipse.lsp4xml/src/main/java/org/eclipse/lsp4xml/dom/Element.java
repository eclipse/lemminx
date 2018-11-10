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

import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * An Element node.
 *
 */
public class Element extends Node implements org.w3c.dom.Element {

	private static final String XMLNS_ATTR = "xmlns";
	private static final String XMLNS_NO_DEFAULT_ATTR = "xmlns:";

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return getTagName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Element#getTagName()
	 */
	@Override
	public String getTagName() {
		return tag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getLocalName()
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getPrefix()
	 */
	@Override
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNamespaceURI()
	 */
	@Override
	public String getNamespaceURI() {
		String prefix = getPrefix();
		boolean hasPrefix = prefix != null && prefix.length() > 0;
		// Try to get xmlns attribute in the element
		String rootElementNamespaceDeclarationName = (hasPrefix) ? XMLNS_NO_DEFAULT_ATTR + prefix // $NON-NLS-1$
				: XMLNS_ATTR; // $NON-NLS-1$
		String rootElementNamespace = rootElementNamespaceDeclarationName != null
				? this.getAttribute(rootElementNamespaceDeclarationName)
				: null;
		if (rootElementNamespace != null) {
			return rootElementNamespace;
		}
		// try to get the namespace in the parent element
		Node parent = getParentNode();
		while (parent != null) {
			if (parent.getNodeType() == Node.ELEMENT_NODE) {
				Element parentElement = ((Element) parent);
				String namespaceURI = hasPrefix ? parentElement.getAttribute(XMLNS_NO_DEFAULT_ATTR + prefix)
						: parentElement.getNamespaceURI();
				if (namespaceURI != null) {
					return namespaceURI;
				}
			}
			parent = parent.getParentNode();
		}
		return null;
	}

	public Collection<String> getAllPrefixes() {
		if (hasAttributes()) {
			Collection<String> prefixes = new ArrayList<>();
			for (Attr attr : getAttributeNodes()) {
				String attributeName = attr.getName();
				if (isNoDefaultXmlns(attributeName)) {
					prefixes.add(extractPrefixFromXmlns(attributeName));
				}
			}
			return prefixes;
		}
		return Collections.emptyList();
	}

	private static String extractPrefixFromXmlns(String attributeName) {
		if (isDefaultXmlns(attributeName)) {
			return attributeName.substring(XMLNS_ATTR.length(), attributeName.length());
		}
		return attributeName.substring(XMLNS_NO_DEFAULT_ATTR.length(), attributeName.length());
	}

	/**
	 * Returns the xmlns prefix from the given namespave URI and null otherwise.
	 * 
	 * @param namespaceURI the namespace
	 * @return the xmlns prefix from the given namespave URI and null otherwise.
	 */
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null) {
			return null;
		}
		if (hasAttributes()) {
			for (Attr attr : getAttributeNodes()) {
				String attributeName = attr.getName();
				if (isXmlns(attributeName)) {
					String namespace = attr.getValue();
					if (namespace != null && namespace.equals(namespaceURI)) {
						if (isDefaultXmlns(attributeName)) {
							// xmlns="http://"
							return "";
						}
						// xmlns:xxx="http://"
						return extractPrefixFromXmlns(attributeName);
					}
				}
			}
		}
		// try to get the prefix in the parent element
		Node parent = getParentNode();
		while (parent != null) {
			if (parent.getNodeType() == Node.ELEMENT_NODE) {
				Element parentElement = ((Element) parent);
				String prefix = parentElement.getPrefix(namespaceURI);
				if (prefix != null) {
					return prefix;
				}
			}
			parent = parent.getParentNode();
		}
		return null;
	}

	/**
	 * Returns true if attribute name is a xmlns attribute and false otherwise.
	 * 
	 * @param attributeName
	 * @return true if attribute name is a xmlns attribute and false otherwise.
	 */
	private static boolean isXmlns(String attributeName) {
		return attributeName.startsWith(XMLNS_ATTR);
	}

	/**
	 * Returns true if attribute name is the default xmlns attribute and false
	 * otherwise.
	 * 
	 * @param attributeName
	 * @return true if attribute name is the default xmlns attribute and false
	 *         otherwise.
	 */
	private static boolean isDefaultXmlns(String attributeName) {
		return attributeName.equals(XMLNS_ATTR);
	}

	/**
	 * Returns true if attribute name is the no default xmlns attribute and false
	 * otherwise.
	 * 
	 * @param attributeName
	 * @return true if attribute name is the no default xmlns attribute and false
	 *         otherwise.
	 */
	private static boolean isNoDefaultXmlns(String attributeName) {
		return attributeName.startsWith(XMLNS_NO_DEFAULT_ATTR);
	}

	public String getNamespaceURI(String prefix) {
		if (prefix == null || prefix.isEmpty()) {
			return getNamespaceURI();
		}
		return getAttribute(XMLNS_NO_DEFAULT_ATTR + prefix);
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

	@Override
	public String getAttributeNS(String arg0, String arg1) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attr getAttributeNode(String name) {
		return super.getAttributeNode(name);
	}

	@Override
	public Attr getAttributeNodeNS(String arg0, String arg1) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeList getElementsByTagName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeList getElementsByTagNameNS(String arg0, String arg1) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeInfo getSchemaTypeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAttributeNS(String arg0, String arg1) throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String arg0) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttributeNS(String arg0, String arg1) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public Attr removeAttributeNode(org.w3c.dom.Attr arg0) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttributeNS(String arg0, String arg1, String arg2) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public Attr setAttributeNode(org.w3c.dom.Attr arg0) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attr setAttributeNodeNS(org.w3c.dom.Attr arg0) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIdAttribute(String arg0, boolean arg1) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIdAttributeNS(String arg0, String arg1, boolean arg2) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIdAttributeNode(org.w3c.dom.Attr arg0, boolean arg1) throws DOMException {
		// TODO Auto-generated method stub

	}

}
