/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.lsp4xml.utils.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * An Element node.
 *
 */
public class DOMElement extends DOMNode implements org.w3c.dom.Element {

	private static final String XMLNS_ATTR = "xmlns";
	private static final String XMLNS_NO_DEFAULT_ATTR = "xmlns:";

	String tag;
	boolean selfClosed;
	
	//DomElement.start == startTagOpenOffset
	int startTagOpenOffset = NULL_VALUE; // |<root>
	int startTagCloseOffset = NULL_VALUE; // <root |>

	int endTagOpenOffset = NULL_VALUE; // <root> |</root >
	int endTagCloseOffset = NULL_VALUE;// <root> </root |>
	//DomElement.end = <root> </root>| , is always scanner.getTokenEnd()

	public DOMElement(int start, int end, DOMDocument ownerDocument) {
		super(start, end, ownerDocument);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public short getNodeType() {
		return DOMNode.ELEMENT_NODE;
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
		boolean hasPrefix = !StringUtils.isEmpty(prefix);
		// Try to get xmlns attribute in the element
		String rootElementNamespaceDeclarationName = (hasPrefix) ? XMLNS_NO_DEFAULT_ATTR + prefix // $NON-NLS-1$
				: XMLNS_ATTR; // $NON-NLS-1$
		String rootElementNamespace = this.getAttribute(rootElementNamespaceDeclarationName);
		if (!StringUtils.isEmpty(rootElementNamespace)) {
			return rootElementNamespace;
		}
		// try to get the namespace in the parent element
		DOMNode parent = getParentNode();
		while (parent != null) {
			if (parent.getNodeType() == DOMNode.ELEMENT_NODE) {
				DOMElement parentElement = ((DOMElement) parent);
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
			for (DOMAttr attr : getAttributeNodes()) {
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
			for (DOMAttr attr : getAttributeNodes()) {
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
		DOMNode parent = getParentNode();
		while (parent != null) {
			if (parent.getNodeType() == DOMNode.ELEMENT_NODE) {
				DOMElement parentElement = ((DOMElement) parent);
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


	/**
	 * Will traverse backwards from the start offset
	 * returning an offset of the given character if it's found 
	 * before another character. Whitespace is ignored.
	 * 
	 * Returns null if the character is not found.
	 * 
	 * The initial value for the start offset is not included.
	 * So have the offset 1 position after the character you want
	 * to start at.
	 */
	public Integer endsWith(char c, int startOffset) {
		String text = this.getOwnerDocument().getText();
		if(startOffset > text.length() || startOffset < 0) {
			return null;
		}
		startOffset--;
		while(startOffset >= 0) {
			char current = text.charAt(startOffset);
			if(Character.isWhitespace(current)) {
				startOffset--;
				continue;
			}
			if(current != c) {
				return null;
			}
			return startOffset;
		}
		return null;
	}

	public Integer isNextChar(char c, int startOffset) {
		String text = this.getOwnerDocument().getText();
		if(startOffset > text.length() || startOffset < 0) {
			return null;
		}
		
		while(startOffset < text.length()) {
			char current = text.charAt(startOffset);
			if(Character.isWhitespace(current)) {
				startOffset++;
				continue;
			}
			if(current != c) {
				return null;
			}
			return startOffset;
		}
		return null;
	}


	public boolean isSameTag(String tagInLowerCase) {
		return this.tag != null && tagInLowerCase != null && this.tag.length() == tagInLowerCase.length()
				&& this.tag.toLowerCase().equals(tagInLowerCase);
	}

	public boolean isInStartTag(int offset) {
		if (startTagOpenOffset == NULL_VALUE || startTagCloseOffset == NULL_VALUE) {
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
		if (endTagOpenOffset == NULL_VALUE) {
			// case >|
			return false;
		}
		if (offset > endTagOpenOffset && offset < getEnd()) {
			// case <\bean | >
			return true;
		}
		return false;
	}

	/**
	 * Returns the start tag open offset and {@link DOMNode#NULL_VALUE} if it
	 * doesn't exist.
	 * 
	 * @return the start tag open offset and {@link DOMNode#NULL_VALUE} if it
	 *         doesn't exist.
	 */
	public int getStartTagOpenOffset() {
		return startTagOpenOffset;
	}

	/**
	 * Returns the start tag close offset and {@link DOMNode#NULL_VALUE} if it
	 * doesn't exist.
	 * 
	 * @return the start tag close offset and {@link DOMNode#NULL_VALUE} if it
	 *         doesn't exist.
	 */
	public int getStartTagCloseOffset() {
		return startTagCloseOffset;
	}

	/**
	 * Returns the end tag open offset and {@link DOMNode#NULL_VALUE} if it doesn't
	 * exist.
	 * 
	 * @return the end tag open offset and {@link DOMNode#NULL_VALUE} if it doesn't
	 *         exist.
	 */
	public int getEndTagOpenOffset() {
		return endTagOpenOffset;
	}

	
	/**
	 * Returns the end tag close offset and {@link DOMNode#NULL_VALUE} if it doesn't
	 * exist.
	 * 
	 * @return the end tag close offset and {@link DOMNode#NULL_VALUE} if it doesn't
	 *         exist.
	 */
	public int getEndTagCloseOffset() {
		return endTagCloseOffset;
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
		return getStartTagOpenOffset() != NULL_VALUE;
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
		return getEndTagOpenOffset() != NULL_VALUE;
	}

	/**
	 * If '>' exists in <root>
	 */
	public boolean isStartTagClosed() {
		return getStartTagCloseOffset() != NULL_VALUE;
	}

	/**
	 * If '>' exists in </root>
	 */
	public boolean isEndTagClosed() {
		return getEndTagCloseOffset() != NULL_VALUE;
	}
	
	/**
	 * If Element has a closing end tag eg: <a> </a> -> true , <a> </b> -> false 
	 */
	@Override
	public boolean isClosed() {
		return super.isClosed();
	}

	@Override
	public String getAttributeNS(String arg0, String arg1) throws DOMException {
		return null;
	}

	@Override
	public DOMAttr getAttributeNode(String name) {
		return super.getAttributeNode(name);
	}

	@Override
	public DOMAttr getAttributeNodeNS(String arg0, String arg1) throws DOMException {
		return null;
	}

	@Override
	public NodeList getElementsByTagName(String arg0) {
		return null;
	}

	@Override
	public NodeList getElementsByTagNameNS(String arg0, String arg1) throws DOMException {
		return null;
	}

	@Override
	public TypeInfo getSchemaTypeInfo() {
		return null;
	}

	@Override
	public boolean hasAttributeNS(String arg0, String arg1) throws DOMException {
		return false;
	}

	@Override
	public void removeAttribute(String arg0) throws DOMException {
	}

	@Override
	public void removeAttributeNS(String arg0, String arg1) throws DOMException {
	}

	@Override
	public DOMAttr removeAttributeNode(org.w3c.dom.Attr arg0) throws DOMException {
		return null;
	}

	@Override
	public void setAttributeNS(String arg0, String arg1, String arg2) throws DOMException {
	}

	@Override
	public DOMAttr setAttributeNode(org.w3c.dom.Attr arg0) throws DOMException {
		return null;
	}

	@Override
	public DOMAttr setAttributeNodeNS(org.w3c.dom.Attr arg0) throws DOMException {
		return null;
	}

	@Override
	public void setIdAttribute(String arg0, boolean arg1) throws DOMException {
	}

	@Override
	public void setIdAttributeNS(String arg0, String arg1, boolean arg2) throws DOMException {
	}

	@Override
	public void setIdAttributeNode(org.w3c.dom.Attr arg0, boolean arg1) throws DOMException {
	}

}
