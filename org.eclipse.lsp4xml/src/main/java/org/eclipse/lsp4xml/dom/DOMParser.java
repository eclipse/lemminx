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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4xml.utils.DOMUtils;

/**
 * Tolerant XML parser.
 *
 */
public class DOMParser {

	private static final Logger LOGGER = Logger.getLogger(DOMParser.class.getName());

	private static final DOMParser INSTANCE = new DOMParser();

	public static DOMParser getInstance() {
		return INSTANCE;
	}

	private DOMParser() {

	}

	public DOMDocument parse(String text, String uri, URIResolverExtensionManager resolverExtensionManager) {
		return parse(new TextDocument(text, uri), resolverExtensionManager);
	}

	public DOMDocument parse(TextDocument document, URIResolverExtensionManager resolverExtensionManager) {
		boolean isDTD = DOMUtils.isDTD(document.getUri());
		String text = document.getText();
		Scanner scanner = XMLScanner.createScanner(text, 0, isDTD);
		DOMDocument xmlDocument = new DOMDocument(document, resolverExtensionManager);

		DOMNode curr = isDTD ? new DOMDocumentType(0, text.length(), xmlDocument) : xmlDocument;
		if (isDTD) {
			xmlDocument.addChild(curr);

			// This DOMDocumentType object is hidden, and just represents the DTD file
			// nothing should affect it's closed status
			curr.closed = true; 
		}
		DOMNode lastClosed = curr;
		DOMAttr attr = null;
		int endTagOpenOffset = -1;
		String pendingAttribute = null;
		boolean isInitialDeclaration = true; // A declaration can have multiple internal declarations
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			switch (token) {
			case StartTagOpen: {
				if(!curr.isClosed() && curr.parent != null) {
					//The next node's parent is not closed at this point
					//so the node's parent (curr) will have its end position updated
					//to the beginning of this new node.
					curr.end = scanner.getTokenOffset();
				}
				if((curr.isClosed()) || (curr.isDoctype() && curr.parent != null)) {
					//The next node is being considered a child of 'curr'
					//and if 'curr' is already closed then it was not updated properly (mostlikey EndTagClose was not triggered).
					curr = curr.parent;
				}
				DOMElement child = xmlDocument.createElement(scanner.getTokenOffset(), scanner.getTokenEnd());
				child.startTagOpenOffset = scanner.getTokenOffset();
				curr.addChild(child);
				curr = child;
				break;
			}

			case StartTag: {
				DOMElement element = (DOMElement) curr;
				element.tag = scanner.getTokenText();
				curr.end = scanner.getTokenEnd();
				break;
			}

			
			case StartTagClose:
				if (curr.isElement()) {
					DOMElement element = (DOMElement) curr;
					curr.end = scanner.getTokenEnd(); // might be later set to end tag position
					element.startTagCloseOffset = scanner.getTokenOffset();

					//never enters isEmptyElement() is always false
					if (element.getTagName() != null && isEmptyElement(element.getTagName()) && curr.parent != null) {
						curr.closed = true;
						curr = curr.parent;
					}
				} else if (curr.isProcessingInstruction() || curr.isProlog()) {
					DOMProcessingInstruction element = (DOMProcessingInstruction) curr;
					curr.end = scanner.getTokenEnd(); // might be later set to end tag position
					element.startTagClose = true;
					if (element.getTarget() != null && isEmptyElement(element.getTarget()) && curr.parent != null) {
						curr.closed = true;
						curr = curr.parent;
					}
				}
				curr.end = scanner.getTokenEnd();
				break;

			case EndTagOpen:
				endTagOpenOffset = scanner.getTokenOffset();
				curr.end = scanner.getTokenOffset();
				break;

			case EndTag:
				// end tag (ex: </root>)
				String closeTag = scanner.getTokenText().toLowerCase();
				DOMNode current = curr;

				/**
				eg: <a><b><c></d> will set a,b,c end position to the start of |</d>
				*/
				while (!(curr.isElement() && ((DOMElement) curr).isSameTag(closeTag)) && curr.parent != null) {
					curr.end = endTagOpenOffset;
					curr = curr.parent;
				}
				if (curr != xmlDocument) {
					curr.closed = true;
					if (curr.isElement()) {
						((DOMElement) curr).endTagOpenOffset = endTagOpenOffset;
					} else if (curr.isProcessingInstruction() || curr.isProlog()) {
						((DOMProcessingInstruction) curr).endTagOpenOffset = endTagOpenOffset;
					}
					curr.end = scanner.getTokenEnd();
				} else {
					// element open tag not found (ex: <root>) add a fake element which only has an
					// end tag (no start tag).
					DOMElement element = xmlDocument.createElement(scanner.getTokenOffset() - 2, scanner.getTokenEnd());
					element.endTagOpenOffset = endTagOpenOffset;
					element.tag = closeTag;
					current.addChild(element);
					curr = element;
				}
				break;

			case StartTagSelfClose:
				if (curr.parent != null) {
					curr.closed = true;
					((DOMElement) curr).selfClosed = true;
					curr.end = scanner.getTokenEnd();
					lastClosed = curr;
					curr = curr.parent;
				}
				break;

			case EndTagClose:
				if (curr.parent != null) {
					curr.end = scanner.getTokenEnd();
					lastClosed = curr;
					if(lastClosed.isElement()) {
						((DOMElement) curr).endTagCloseOffset = scanner.getTokenOffset();
					}
					curr = curr.parent;
				}
				break;

			case AttributeName: {
				pendingAttribute = scanner.getTokenText();
				attr = new DOMAttr(pendingAttribute, scanner.getTokenOffset(),
						scanner.getTokenOffset() + pendingAttribute.length(), curr);
				curr.setAttributeNode(attr);
				curr.end = scanner.getTokenEnd();
				break;
			}

			case AttributeValue: {
				String value = scanner.getTokenText();
				if (curr.hasAttributes() && attr != null) {
					attr.setValue(value, scanner.getTokenOffset(), scanner.getTokenOffset() + value.length());
				}
				pendingAttribute = null;
				attr = null;
				curr.end = scanner.getTokenEnd();
				break;
			}

			case CDATATagOpen: {
				DOMCDATASection cdataNode = xmlDocument.createCDataSection(scanner.getTokenOffset(), text.length());
				curr.addChild(cdataNode);
				curr = cdataNode;
				break;
			}

			case CDATAContent: {
				DOMCDATASection cdataNode = (DOMCDATASection) curr;
				cdataNode.startContent = scanner.getTokenOffset();
				cdataNode.endContent = scanner.getTokenEnd();
				curr.end = scanner.getTokenEnd();
				break;
			}

			case CDATATagClose: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case StartPrologOrPI: {
				DOMProcessingInstruction prologOrPINode = xmlDocument
						.createProcessingInstruction(scanner.getTokenOffset(), text.length());
				curr.addChild(prologOrPINode);
				curr = prologOrPINode;
				break;
			}

			case PIName: {
				DOMProcessingInstruction processingInstruction = ((DOMProcessingInstruction) curr);
				processingInstruction.target = scanner.getTokenText();
				processingInstruction.processingInstruction = true;
				break;
			}

			case PrologName: {
				DOMProcessingInstruction processingInstruction = ((DOMProcessingInstruction) curr);
				processingInstruction.target = scanner.getTokenText();
				processingInstruction.prolog = true;
				break;
			}

			case PIContent: {
				DOMProcessingInstruction processingInstruction = (DOMProcessingInstruction) curr;
				processingInstruction.startContent = scanner.getTokenOffset();
				processingInstruction.endContent = scanner.getTokenEnd();
				break;
			}

			case PIEnd:
			case PrologEnd: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case StartCommentTag: {
				if((curr.isClosed()) || (curr.isDoctype() && curr.parent != null)) {
					curr = curr.parent;
				}
				DOMComment comment = xmlDocument.createComment(scanner.getTokenOffset(), text.length());
				if(curr.parent != null && curr.parent.isDoctype()) {
					curr.parent.addChild(comment);
				}
				curr.addChild(comment);
				curr = comment;
				try {
					int endLine = document.positionAt(lastClosed.end).getLine();
					int startLine = document.positionAt(curr.start).getLine();
					if (endLine == startLine && lastClosed.end <= curr.start) {
						comment.commentSameLineEndTag = true;
					}
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, "XMLParser StartCommentTag bad offset in document", e);
				}
				break;
			}

			case Comment: {
				DOMComment comment = (DOMComment) curr;
				comment.startContent = scanner.getTokenOffset();
				comment.endContent = scanner.getTokenEnd();
				break;
			}

			case EndCommentTag: {
				curr.end = scanner.getTokenEnd();
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case Content: {
				// FIXME: don't use getTokenText (substring) to know if the content is only
				// spaces or line feed (scanner should know that).
				String content = scanner.getTokenText();
				if (content.trim().length() == 0) { // if string is only whitespaces
					break;
				}
				if (curr instanceof DTDDeclNode) {
					curr.end = scanner.getTokenOffset() - 1;
					if(!curr.isDoctype()) {
						curr = curr.getParentNode();
					}	
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				DOMText textNode = xmlDocument.createText(start, end);
				textNode.closed = true;
				curr.addChild(textNode);
				break;
			}

			// DTD

			case DTDStartDoctypeTag: {
				DOMDocumentType doctype = xmlDocument.createDocumentType(scanner.getTokenOffset(), text.length());
				curr.addChild(doctype);
				doctype.parent = curr;
				curr = doctype;
				break;
			}

			case DTDDoctypeName: {
				DOMDocumentType doctype = (DOMDocumentType) curr;
				doctype.setName(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDDocTypeKindPUBLIC: {
				DOMDocumentType doctype = (DOMDocumentType) curr;
				doctype.setKind(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDDocTypeKindSYSTEM: {
				DOMDocumentType doctype = (DOMDocumentType) curr;
				doctype.setKind(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDDoctypePublicId: {
				DOMDocumentType doctype = (DOMDocumentType) curr;
				doctype.setPublicId(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDDoctypeSystemId: {
				DOMDocumentType doctype = (DOMDocumentType) curr;
				doctype.setSystemId(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDStartInternalSubset: {
				DOMDocumentType doctype = (DOMDocumentType) curr;
				doctype.internalSubsetStart = scanner.getTokenOffset();
				break;
			}

			case DTDEndInternalSubset: {
				if (!curr.isDoctype()) {
					curr.end = scanner.getTokenOffset() - 1;
					curr = curr.getParentNode();
				}
				
				DOMDocumentType doctype = (DOMDocumentType) curr;
				doctype.internalSubsetEnd = scanner.getTokenEnd();
				break;
			}

			case DTDStartElement: {
				//If previous 'curr' was an unclosed ENTITY, ELEMENT, or ATTLIST
				if (!curr.isDoctype()) {
					curr.end = scanner.getTokenOffset() - 1;
					curr = curr.getParentNode();
				}
				
				DTDElementDecl child = new DTDElementDecl(scanner.getTokenOffset(), text.length(),
						(DOMDocumentType) curr);
				curr.addChild(child);
				curr = child;
				break;
			}

			case DTDElementDeclName: {
				DTDElementDecl element = (DTDElementDecl) curr;
				element.nameStart = scanner.getTokenOffset();
				element.nameEnd = scanner.getTokenEnd();
				break;
			}

			case DTDElementCategory: {
				DTDElementDecl element = (DTDElementDecl) curr;
				element.categoryStart = scanner.getTokenOffset();
				element.categoryEnd = scanner.getTokenEnd();
				break;
			}

			case DTDStartElementContent: {
				DTDElementDecl element = (DTDElementDecl) curr;
				element.contentStart = scanner.getTokenOffset();
				break;
			}

			case DTDElementContent: {
				DTDElementDecl element = (DTDElementDecl) curr;
				element.contentEnd = scanner.getTokenEnd();
			}

			case DTDEndElementContent: {
				DTDElementDecl element = (DTDElementDecl) curr;
				element.contentEnd = scanner.getTokenEnd();
				break;
			}

			case DTDStartAttlist: {
				if (!curr.isDoctype()) { // If previous DTD Decl was unclosed
					curr.end = scanner.getTokenOffset() - 1;
					curr = curr.getParentNode();
				}
				DTDAttlistDecl child = new DTDAttlistDecl(scanner.getTokenOffset(), text.length(),
						(DOMDocumentType) curr);
		
				isInitialDeclaration = true;
				curr.addChild(child);
				curr = child;
				
				break;
			}

			case DTDAttlistElementName: {
				DTDAttlistDecl attribute = (DTDAttlistDecl) curr;
				attribute.elementNameStart = scanner.getTokenOffset();
				attribute.elementNameEnd = scanner.getTokenEnd();
				break;
			}

			
			case DTDAttlistAttributeName: {
				DTDAttlistDecl attribute = (DTDAttlistDecl) curr;
				if(isInitialDeclaration == false) {
					// All additional declarations are created as new DTDAttlistDecl's
					DTDAttlistDecl child = new DTDAttlistDecl(-1, -1, attribute.getParentDocumentType()); // Wont use these values
					attribute.addAdditionalAttDecl(child);
					child.parent = attribute;

					attribute = child;
					curr = child;
				}
				
				attribute.attributeNameStart = scanner.getTokenOffset();
				attribute.attributeNameEnd = scanner.getTokenEnd();
				break;
			}

			case DTDAttlistAttributeType: {
				DTDAttlistDecl attribute = (DTDAttlistDecl) curr;
				attribute.attributeTypeStart = scanner.getTokenOffset();
				attribute.attributeTypeEnd = scanner.getTokenEnd();
				break;
			}

			case DTDAttlistAttributeValue: {
				DTDAttlistDecl attribute = (DTDAttlistDecl) curr;
				attribute.attributeValueStart = scanner.getTokenOffset();
				attribute.attributeValueEnd = scanner.getTokenEnd();

				if(attribute.parent.isDTDAttListDecl()) { // Is not the root/main ATTLIST node
					curr = attribute.parent;
				}
				else {
					isInitialDeclaration = false;
				}
				break;
			}
			
			
			case DTDStartEntity: {
				if (!curr.isDoctype()) { // If previous DTD Decl was unclosed
					curr.end = scanner.getTokenOffset() - 1;
					curr = curr.getParentNode();
				}
				DTDEntityDecl child = new DTDEntityDecl(scanner.getTokenOffset(), text.length(), (DOMDocumentType) curr);
				curr.addChild(child);
				curr = child;
				break;
			}

			case DTDEntityPercent: {
				DTDEntityDecl entity = (DTDEntityDecl) curr;
				entity.percentStart = scanner.getTokenOffset();
				entity.percentEnd = scanner.getTokenEnd();
				break;
			}

			case DTDEntityName : {
				DTDEntityDecl entity = (DTDEntityDecl) curr;
				entity.nameStart = scanner.getTokenOffset();
				entity.nameEnd = scanner.getTokenEnd();
				break;
			}

			case DTDEntityValue : {
				DTDEntityDecl entity = (DTDEntityDecl) curr;
				entity.valueStart = scanner.getTokenOffset();
				entity.valueEnd = scanner.getTokenEnd();
				break;
			}

			case DTDEntityKindPUBLIC:
			case DTDEntityKindSYSTEM: {
				DTDEntityDecl entity = (DTDEntityDecl) curr;
				entity.kindStart = scanner.getTokenOffset();
				entity.kindEnd = scanner.getTokenEnd();
				break;
			}

			case DTDEntityPublicId: {
				DTDEntityDecl entity = (DTDEntityDecl) curr;
				entity.publicIdStart = scanner.getTokenOffset();
				entity.publicIdEnd = scanner.getTokenEnd();
				break;
			}

			case DTDEntitySystemId: {
				DTDEntityDecl entity = (DTDEntityDecl) curr;
				entity.systemIdStart = scanner.getTokenOffset();
				entity.systemIdEnd = scanner.getTokenEnd();
				break;
			}

			

			case DTDStartNotation: {
				if (!curr.isDoctype()) { // If previous DTD Decl was unclosed
					curr.end = scanner.getTokenOffset() - 1;
					curr = curr.getParentNode();
				}
				DTDNotationDecl child = new DTDNotationDecl(scanner.getTokenOffset(), text.length(), (DOMDocumentType) curr);
				curr.addChild(child);
				curr = child;
				isInitialDeclaration = true;
				break;
			}

			case DTDNotationName: {
				DTDNotationDecl notation = (DTDNotationDecl) curr;
				notation.setName(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDNotationKindPUBLIC: {
				DTDNotationDecl notation = (DTDNotationDecl) curr;
				notation.setKind(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDNotationKindSYSTEM: {
				DTDNotationDecl notation = (DTDNotationDecl) curr;
				notation.setKind(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDNotationPublicId: {
				DTDNotationDecl notation = (DTDNotationDecl) curr;
				notation.setPublicId(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDNotationSystemId: {
				DTDNotationDecl notation = (DTDNotationDecl) curr;
				notation.setSystemId(scanner.getTokenOffset(), scanner.getTokenEnd());
				break;
			}

			case DTDEndTag: {
				if ((curr.isDTDElementDecl() || curr.isDTDAttListDecl() || curr.isDTDEntityDecl() || curr.isDTDNotationDecl()) && curr.parent != null) {
					if(curr.isDTDNotationDecl() && curr.parent.isDoctype() == false) {
						curr = curr.parent;
					}
					curr.end = scanner.getTokenEnd();
					curr.closed = true;
					curr = curr.parent;
				}
				break;
			}
			
			case DTDEndDoctypeTag: {
				((DOMDocumentType) curr).setEnd(scanner.getTokenEnd());
				curr.closed = true;
				curr = curr.parent;
				break;
			}

			case DTDUnrecognizedParameters: {
				DTDDeclNode node = (DTDDeclNode) curr;
				node.unrecognizedStart = scanner.getTokenOffset();
				node.unrecognizedEnd = scanner.getTokenEnd();
				break;
			}

			default:
			}
			token = scanner.scan();
		}
		while (curr.parent != null ) {
			curr.end = text.length();
			curr = curr.parent;
		}
		return xmlDocument;
	}

	private static boolean isEmptyElement(String tag) {
		return false;
	}

}
