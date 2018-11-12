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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.parser.Constants;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4xml.utils.StringUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NodeList;

/**
 * XML document.
 *
 */
public class XMLDocument extends Node implements Document {

	private SchemaLocation schemaLocation;
	private NoNamespaceSchemaLocation noNamespaceSchemaLocation;
	private boolean referencedGrammarInitialized;
	private final URIResolverExtensionManager resolverExtensionManager;

	private final TextDocument textDocument;
	private boolean hasNamespaces;
	private boolean hasGrammar;
	private Map<String, String> externalSchemaLocation;

	public XMLDocument(TextDocument textDocument, URIResolverExtensionManager resolverExtensionManager) {
		super(0, textDocument.getText().length(), null);
		this.textDocument = textDocument;
		this.resolverExtensionManager = resolverExtensionManager;
		this.referencedGrammarInitialized = false;
	}

	public List<Node> getRoots() {
		return super.getChildren();
	}

	public Position positionAt(int position) throws BadLocationException {
		return textDocument.positionAt(position);
	}

	public int offsetAt(Position position) throws BadLocationException {
		return textDocument.offsetAt(position);
	}

	public String lineText(int lineNumber) throws BadLocationException {
		return textDocument.lineText(lineNumber);
	}

	public String lineDelimiter(int lineNumber) throws BadLocationException {
		return textDocument.lineDelimiter(lineNumber);
	}

	public LineIndentInfo getLineIndentInfo(int lineNumber) throws BadLocationException {
		String lineText = lineText(lineNumber);
		String lineDelimiter = lineDelimiter(lineNumber);
		String whitespacesIndent = StringUtils.getStartWhitespaces(lineText);
		return new LineIndentInfo(lineDelimiter, whitespacesIndent);
	}

	/**
	 * Returns the element name on the left of the given position and null
	 * otherwise.
	 * 
	 * @param textOffset
	 * @return the element name on the left of the given position and null
	 *         otherwise.
	 */
	public Range getElementNameRangeAt(int textOffset) {
		return textDocument.getWordRangeAt(textOffset, Constants.ELEMENT_NAME_REGEX);
	}

	/**
	 * Returns the declared "xsi:schemaLocation" and null otherwise.
	 * 
	 * @return the declared "xsi:schemaLocation" and null otherwise.
	 */
	public SchemaLocation getSchemaLocation() {
		initializeReferencedGrammarIfNeeded();
		return schemaLocation;
	}

	/**
	 * Returns the declared "xsi:noNamespaceSchemaLocation" and null otherwise.
	 * 
	 * @return the declared "xsi:noNamespaceSchemaLocation" and null otherwise.
	 */
	public NoNamespaceSchemaLocation getNoNamespaceSchemaLocation() {
		initializeReferencedGrammarIfNeeded();
		return noNamespaceSchemaLocation;
	}

	public String getNamespaceURI() {
		Element documentElement = getDocumentElement();
		return documentElement != null ? documentElement.getNamespaceURI() : null;
	}

	/**
	 * Returns the text content of the XML document.
	 * 
	 * @return the text content of the XML document.
	 */
	public String getText() {
		return textDocument.getText();
	}

	public TextDocument getTextDocument() {
		return textDocument;
	}

	/**
	 * Returns true id document defines namespaces (with xmlns) and false otherwise.
	 * 
	 * @return true id document defines namespaces (with xmlns) and false otherwise.
	 */
	public boolean hasNamespaces() {
		initializeReferencedGrammarIfNeeded();
		return hasNamespaces;
	}

	/**
	 * Returns true if the document is bound to a grammar and false otherwise.
	 * 
	 * @return true if the document is bound to a grammar and false otherwise.
	 */
	public boolean hasGrammar() {
		initializeReferencedGrammarIfNeeded();
		return hasGrammar;
	}

	public Map<String, String> getExternalSchemaLocation() {
		initializeReferencedGrammarIfNeeded();
		return externalSchemaLocation;
	}

	private void initializeReferencedGrammarIfNeeded() {
		if (!referencedGrammarInitialized) {
			initializeReferencedGrammar();
		}
	}

	/**
	 * Initialize refrenced grammar information (XML Dchema, DTD)/.
	 */
	private synchronized void initializeReferencedGrammar() {
		if (referencedGrammarInitialized) {
			return;
		}
		// Check if XML Schema reference is declared
		initializeSchemaLocation();
		if (!hasGrammar) {
			// Check if there are a DTD
			hasGrammar = getDoctype() != null;
		}
		referencedGrammarInitialized = true;
	}

	/**
	 * Initialize namespaces and schema location declaration .
	 */
	private void initializeSchemaLocation() {
		// Get root element
		Element documentElement = getDocumentElement();
		if (documentElement == null) {
			return;
		}
		String schemaInstancePrefix = null;
		// Search if document element root declares namespace with "xmlns".
		if (documentElement.hasAttributes()) {

			for (Attr attr : documentElement.getAttributeNodes()) {
				String attributeName = attr.getName();
				if (attributeName != null && attributeName.equals("xmlns") || attributeName.startsWith("xmlns:")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					hasNamespaces = true;
				}
				String attributeValue = documentElement.getAttribute(attributeName);
				if (attributeValue != null && attributeValue.startsWith("http://www.w3.org/") //$NON-NLS-1$
						&& attributeValue.endsWith("/XMLSchema-instance")) //$NON-NLS-1$
				{
					schemaInstancePrefix = attributeName.equals("xmlns") ? "" : getUnprefixedName(attributeName); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (schemaInstancePrefix != null) {
				noNamespaceSchemaLocation = createNoNamespaceSchemaLocation(documentElement, schemaInstancePrefix);
				if (noNamespaceSchemaLocation == null) {
					schemaLocation = createSchemaLocation(documentElement, schemaInstancePrefix);
				}
			}
			hasGrammar = noNamespaceSchemaLocation != null || schemaLocation != null;
		}
		if (!hasGrammar && resolverExtensionManager != null) {
			// None grammar found with standard mean, check if it some components like XML
			// file associations bind this XML document to a grammar with external schema
			// location.
			try {
				externalSchemaLocation = resolverExtensionManager.getExternalSchemaLocation(new URI(getDocumentURI()));
				hasGrammar = externalSchemaLocation != null;
			} catch (URISyntaxException e) {
				// Do nothing
			}
			if (!hasGrammar) {
				// None grammar found with standard mean and external schema location, check if
				// it some components like XML
				// Catalog, XSL and XSD resolvers, etc bind this XML document to a grammar.
				String namespaceURI = documentElement.getNamespaceURI();
				hasGrammar = resolverExtensionManager.resolve(getDocumentURI(), namespaceURI, null) != null;
			}
		}
	}

	private SchemaLocation createSchemaLocation(Node root, String schemaInstancePrefix) {
		String value = root.getAttribute(getPrefixedName(schemaInstancePrefix, "schemaLocation"));
		if (value == null) {
			return null;
		}
		return new SchemaLocation(root.getOwnerDocument().getDocumentURI(), value);
	}

	private NoNamespaceSchemaLocation createNoNamespaceSchemaLocation(Node root, String schemaInstancePrefix) {
		Attr attr = root.getAttributeNode(getPrefixedName(schemaInstancePrefix, "noNamespaceSchemaLocation"));
		if (attr == null || attr.getValue() == null) {
			return null;
		}
		return new NoNamespaceSchemaLocation(root.getOwnerDocument().getDocumentURI(), attr);
	}

	private static String getUnprefixedName(String name) {
		int index = name.indexOf(":"); //$NON-NLS-1$
		if (index != -1) {
			name = name.substring(index + 1);
		}
		return name;
	}

	private static String getPrefixedName(String prefix, String localName) {
		return prefix != null && prefix.length() > 0 ? prefix + ":" + localName : localName; //$NON-NLS-1$
	}

	public Element createElement(int start, int end) {
		return new Element(start, end, this);
	}

	public CDataSection createCDataSection(int start, int end) {
		return new CDataSection(start, end, this);
	}

	public ProcessingInstruction createProcessingInstruction(int start, int end) {
		return new ProcessingInstruction(start, end, this);
	}

	public Comment createComment(int start, int end) {
		return new Comment(start, end, this);
	}

	public Text createText(int start, int end) {
		return new Text(start, end, this);
	}

	public DocumentType createDocumentType(int start, int end) {
		return new DocumentType(start, end, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	@Override
	public short getNodeType() {
		return Node.DOCUMENT_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return "#document";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getDocumentElement()
	 */
	@Override
	public Element getDocumentElement() {
		List<Node> roots = getRoots();
		if (roots != null) {
			for (Node node : roots) {
				if (node.isElement()) {
					return (Element) node;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getDoctype()
	 */
	@Override
	public DocumentType getDoctype() {
		List<Node> roots = getRoots();
		if (roots != null) {
			for (Node node : roots) {
				if (node.isDoctype()) {
					return (DocumentType) node;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.lsp4xml.dom.Node#getOwnerDocument()
	 */
	@Override
	public XMLDocument getOwnerDocument() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getDocumentURI()
	 */
	@Override
	public String getDocumentURI() {
		return textDocument.getUri();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#setDocumentURI(java.lang.String)
	 */
	@Override
	public void setDocumentURI(String documentURI) {
		textDocument.setUri(documentURI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#adoptNode(org.w3c.dom.Node)
	 */
	@Override
	public Node adoptNode(org.w3c.dom.Node source) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createAttribute(java.lang.String)
	 */
	@Override
	public Attr createAttribute(String name) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createAttributeNS(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createCDATASection(java.lang.String)
	 */
	@Override
	public CDATASection createCDATASection(String data) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createComment(java.lang.String)
	 */
	@Override
	public Comment createComment(String data) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createDocumentFragment()
	 */
	@Override
	public DocumentFragment createDocumentFragment() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createElement(java.lang.String)
	 */
	@Override
	public Element createElement(String tagName) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createElementNS(java.lang.String, java.lang.String)
	 */
	@Override
	public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createEntityReference(java.lang.String)
	 */
	@Override
	public EntityReference createEntityReference(String name) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createProcessingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#createTextNode(java.lang.String)
	 */
	@Override
	public Text createTextNode(String data) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getDomConfig()
	 */
	@Override
	public DOMConfiguration getDomConfig() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getElementById(java.lang.String)
	 */
	@Override
	public Element getElementById(String elementId) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getElementsByTagName(java.lang.String)
	 */
	@Override
	public NodeList getElementsByTagName(String tagname) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getElementsByTagNameNS(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getImplementation()
	 */
	@Override
	public DOMImplementation getImplementation() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getInputEncoding()
	 */
	@Override
	public String getInputEncoding() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getStrictErrorChecking()
	 */
	@Override
	public boolean getStrictErrorChecking() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getXmlEncoding()
	 */
	@Override
	public String getXmlEncoding() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getXmlStandalone()
	 */
	@Override
	public boolean getXmlStandalone() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#getXmlVersion()
	 */
	@Override
	public String getXmlVersion() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#importNode(org.w3c.dom.Node, boolean)
	 */
	@Override
	public Node importNode(org.w3c.dom.Node importedNode, boolean deep) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#normalizeDocument()
	 */
	@Override
	public void normalizeDocument() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#renameNode(org.w3c.dom.Node, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Node renameNode(org.w3c.dom.Node n, String namespaceURI, String qualifiedName) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#setStrictErrorChecking(boolean)
	 */
	@Override
	public void setStrictErrorChecking(boolean strictErrorChecking) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#setXmlStandalone(boolean)
	 */
	@Override
	public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Document#setXmlVersion(java.lang.String)
	 */
	@Override
	public void setXmlVersion(String xmlVersion) throws DOMException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Reset the cached grammar flag.
	 */
	public void resetGrammar() {
		this.referencedGrammarInitialized = false;
	}

	public URIResolverExtensionManager getResolverExtensionManager() {
		return resolverExtensionManager;
	}

}