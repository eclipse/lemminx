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

import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.parser.Constants;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * XML document.
 *
 */
public class XMLDocument extends Node {

	private SchemaLocation schemaLocation;
	private NoNamespaceSchemaLocation noNamespaceSchemaLocation;
	private boolean referencedGrammarInitialized;

	private final TextDocument textDocument;
	private boolean hasNamespaces;
	private boolean hasGrammar;

	public XMLDocument(TextDocument textDocument) {
		super(0, textDocument.getText().length(), null);
		this.textDocument = textDocument;
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

	/**
	 * Returns the document root element and null otherwise.
	 * 
	 * @return the document root element and null otherwise.
	 */
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

	/**
	 * Returns the document type and null otherwise.
	 * 
	 * @return the document type and null otherwise.
	 */
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

	/**
	 * Returns the file URI of the XML document.
	 * 
	 * @return the file URI of the XML document.
	 */
	public String getUri() {
		return textDocument.getUri();
	}

	public TextDocument getTextDocument() {
		return textDocument;
	}

	@Override
	public XMLDocument getOwnerDocument() {
		return this;
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

	@Override
	public short getNodeType() {
		return Node.DOCUMENT_NODE;
	}

	@Override
	public String getNodeName() {
		return "#document";
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
				String attributeValue = documentElement.getAttributeValue(attributeName);
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
			if (!hasGrammar) {
				// None grammar found with standard mean, check if it some components like XML
				// Catalog, XML file associations, etc
				// bind this XML document to a grammar.
				String namespaceURI = documentElement.getNamespaceURI();
				hasGrammar = URIResolverExtensionManager.getInstance().resolve(getUri(), namespaceURI, null) != null;
			}
		}
	}

	private SchemaLocation createSchemaLocation(Node root, String schemaInstancePrefix) {
		String value = root.getAttributeValue(getPrefixedName(schemaInstancePrefix, "schemaLocation"));
		if (value == null) {
			return null;
		}
		return new SchemaLocation(root.getOwnerDocument().getUri(), value);
	}

	private NoNamespaceSchemaLocation createNoNamespaceSchemaLocation(Node root, String schemaInstancePrefix) {
		Attr attr = root.getAttributeNode(getPrefixedName(schemaInstancePrefix, "noNamespaceSchemaLocation"));
		if (attr == null || attr.getValue() == null) {
			return null;
		}
		return new NoNamespaceSchemaLocation(root.getOwnerDocument().getUri(), attr);
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

}