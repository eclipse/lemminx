/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMCDATASection;
import org.eclipse.lsp4xml.dom.DOMComment;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMDocumentType;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.eclipse.lsp4xml.dom.DOMProcessingInstruction;
import org.eclipse.lsp4xml.dom.DOMText;
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
import org.eclipse.lsp4xml.dom.DTDDeclNode;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.StringUtils;
import org.eclipse.lsp4xml.utils.XMLBuilder;

/**
 * XML formatter support.
 *
 */
class XMLFormatter {

	private static final Logger LOGGER = Logger.getLogger(XMLFormatter.class.getName());

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLFormatter(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends TextEdit> format(TextDocument document, Range range, XMLFormattingOptions formattingOptions) {
		try {
			// Compute start/end offset range
			int start = -1;
			int end = -1;
			if (range == null) {
				start = 0;
				end = document.getText().length();
			} else {
				start = document.offsetAt(range.getStart());
				end = document.offsetAt(range.getEnd());
			}
			Position startPosition = document.positionAt(start);
			Position endPosition = document.positionAt(end);

			// Parse the content to format to create an XML document with full data (CData,
			// comments, etc)
			String text = document.getText().substring(start, end);
			DOMDocument doc = DOMParser.getInstance().parse(text, document.getUri(), null);

			// Format the content
			XMLBuilder xml = new XMLBuilder(formattingOptions, "", document.lineDelimiter(startPosition.getLine()));
			format(doc, 0, end, xml);

			// Returns LSP list of TextEdits
			Range r = new Range(startPosition, endPosition);
			List<TextEdit> edits = new ArrayList<>();
			edits.add(new TextEdit(r, xml.toString()));
			return edits;

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Formatting failed due to BadLocation from 'range' parameter", e);
		}
		return null;
	}

	private void format(DOMNode node, int level, int end, XMLBuilder xml) {
		if (node.getNodeType() != DOMNode.DOCUMENT_NODE) {
			boolean doLineFeed;
			if(node.getOwnerDocument().isDTD()) {
				doLineFeed = false;
			} else {
				doLineFeed = !(node.isComment() && ((DOMComment) node).isCommentSameLineEndTag())
					&& (!isPreviousNodeType(node, DOMNode.TEXT_NODE) || xml.isJoinContentLines())
					&& (!node.isText() || ((xml.isJoinContentLines() && !isFirstChildNode(node))));
			}

			if (level > 0 && doLineFeed) {
				// add new line + indent
				xml.linefeed();
				xml.indent(level);
			}
			// generate start element
			if (node.isElement()) {
				DOMElement element = (DOMElement) node;
				String tag = element.getTagName();
				if (element.hasEndTag() && !element.hasStartTag()) {
					// bad element which have not start tag (ex: <\root>)
					xml.endElement(tag, element.isEndTagClosed());
				} else {
					xml.startElement(tag, false);
					if (element.hasAttributes()) {
						// generate attributes
						List<DOMAttr> attributes = element.getAttributeNodes();
						if (attributes.size() == 1) {
							DOMAttr singleAttribute = attributes.get(0);
							xml.addSingleAttribute(singleAttribute.getName(), singleAttribute.getValue());
						} else {
							int attributeIndex = 0;
							for (DOMAttr attr : attributes) {
								String attributeName = attr.getName();
								xml.addAttribute(attributeName, attr.getValue(), attributeIndex, level, tag);
								attributeIndex++;
							}
						}
					}

					if(element.isStartTagClosed()) {
						xml.closeStartElement();	
					}

					boolean hasElements = false;
					if (node.hasChildNodes()) {
						// element has body
						
						
						//startElementClosed = true;
						level++;
						for (DOMNode child : node.getChildren()) {
							boolean textElement = !child.isText();

							hasElements = hasElements | textElement;

							format(child, level, end, xml);
						}
						level--;
					}
					if (element.hasEndTag()) {
						if (hasElements) {
							xml.linefeed();
							xml.indent(level);
						}
						// end tag element is done, only if the element is closed
						// the format, doesn't fix the close tag
						if (element.hasEndTag() && element.getEndTagOpenOffset() <= end) {
							xml.endElement(tag, element.isEndTagClosed());
						} else {
							xml.selfCloseElement();
						}
					} else if (element.isSelfClosed()) {
						xml.selfCloseElement();
						
					}
				}
				return;

			} else if (node.isCDATA()) {
				DOMCDATASection cdata = (DOMCDATASection) node;
				xml.startCDATA();
				xml.addContentCDATA(cdata.getData());
				xml.endCDATA();
			} else if (node.isComment()) {
				DOMComment comment = (DOMComment) node;
				xml.startComment(comment);
				xml.addContentComment(comment.getData());
				xml.endComment();
				if (level == 0) {
					xml.linefeed();
				}
			} else if (node.isProcessingInstruction()) {
				DOMProcessingInstruction processingInstruction = (DOMProcessingInstruction) node;
				xml.startPrologOrPI(processingInstruction.getTarget());
				xml.addContentPI(processingInstruction.getData());
				xml.endPrologOrPI();
				if (level == 0) {
					xml.linefeed();
				}
			} else if (node.isProlog()) {
				DOMProcessingInstruction processingInstruction = (DOMProcessingInstruction) node;
				xml.startPrologOrPI(processingInstruction.getTarget());
				if (node.hasAttributes()) {
					// generate attributes
					String[] attributes = new String[3];
					attributes[0] = "version";
					attributes[1] = "encoding";
					attributes[2] = "standalone";

					for (String name : attributes) {
						String value = node.getAttribute(name);
						if (value == null) {
							continue;
						}
						xml.addSingleAttribute(name, value);
					}
				}
				xml.endPrologOrPI();
				xml.linefeed();
			} else if (node.isText()) {
				DOMText text = (DOMText) node;
				if (text.hasData()) {
					// Generate content
					String content = text.getData();
					if (!content.isEmpty()) {
						xml.addContent(content);
					}

				}
				return;
			} else if (node.isDoctype()) {
				boolean isDTD = node.getOwnerDocument().isDTD();
				DOMDocumentType documentType = (DOMDocumentType) node;
				if(!isDTD) {
					xml.startDoctype();
					ArrayList<DTDDeclParameter> params = documentType.getParameters();
					for (DTDDeclParameter param : params) {
						if(!documentType.isInternalSubset(param)) {
							xml.addParameter(param.getParameter());
						} else {
							xml.startDoctypeInternalSubset();
							xml.linefeed();
							// level + 1 since the 'level' value is the doctype tag's level
							formatDTD(documentType, level + 1, end, xml); 
							xml.linefeed();
							xml.endDoctypeInternalSubset();
						}
					}
					if(documentType.isClosed()) {
						xml.endDoctype();
					}
					xml.linefeed();
				} else {
					formatDTD(documentType, 0, end, xml);
				}
				return;
			}
		} else if (node.hasChildNodes()) {
			// Other nodes kind like root
			for (DOMNode child : node.getChildren()) {
				format(child, level, end, xml);
			}
		}
	}

	private static boolean formatDTD(DOMDocumentType doctype, int level, int end, XMLBuilder xml) {
		DOMNode previous = null;
		for (DOMNode node : doctype.getChildren()) {
			if(previous != null) {
				xml.linefeed();
			} 

			xml.indent(level);
			
			if(node.isText()) {
				xml.addContent(((DOMText)node).getData().trim());
			}
			else {
				if(node.isComment()) {
					DOMComment comment = (DOMComment) node;
					xml.startComment(comment);
					xml.addContentComment(comment.getData());
					xml.endComment();
				}
				else {
					boolean setEndBracketOnNewLine = false;
					DTDDeclNode decl = (DTDDeclNode) node;
					xml.addDeclTagStart(decl);

					if(decl.isDTDAttListDecl()) {
						DTDAttlistDecl attlist = (DTDAttlistDecl) decl;
						ArrayList<DTDAttlistDecl> internalDecls = attlist.getInternalChildren();

						if(internalDecls == null) {
							for (DTDDeclParameter param : decl.getParameters()) {
								xml.addParameter(param.getParameter());	
							}
						}
						else {
							boolean multipleInternalAttlistDecls = false;

							ArrayList<DTDDeclParameter> params = attlist.getParameters();
							DTDDeclParameter param;
							for(int i = 0; i < params.size(); i++) {
								param = params.get(i);
								if(attlist.elementName.equals(param)) {
									xml.addParameter(param.getParameter());
									if(attlist.getParameters().size() > 1) { //has parameters after elementName
										xml.linefeed();
										xml.indent(level + 1);
										setEndBracketOnNewLine = true;
										multipleInternalAttlistDecls = true;
									}
								} else {
									if(multipleInternalAttlistDecls && i == 1) {
										xml.addUnindentedParameter(param.getParameter());
									} else {
										xml.addParameter(param.getParameter());	
									}
									
								}
							}

							for (DTDAttlistDecl attlistDecl : internalDecls) {
								xml.linefeed();
								xml.indent(level + 1);
								params = attlistDecl.getParameters();
								for(int i = 0; i < params.size(); i++) {
									param = params.get(i);
									if(i == 0) {
										xml.addUnindentedParameter(param.getParameter());
									} else {
										xml.addParameter(param.getParameter());	
									}
								}
							}
						}
					} else {
						for (DTDDeclParameter param : decl.getParameters()) {
							xml.addParameter(param.getParameter());	
						}
					}
					if(setEndBracketOnNewLine) {
						xml.linefeed();
						xml.indent(level);
					}
					if(decl.isClosed()) {
						xml.closeStartElement();
					}
				}
			}
			previous = node;
		}
		return true;
	}

	private static boolean isFirstChildNode(DOMNode node) {
		return node.equals(node.getParentNode().getFirstChild());
	}

	private static boolean isPreviousNodeType(DOMNode node, short nodeType) {
		DOMNode previousNode = node.getPreviousSibling();
		return previousNode != null && previousNode.getNodeType() == nodeType;
	}

}
