/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMCDATASection;
import org.eclipse.lemminx.dom.DOMComment;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.dom.DOMProcessingInstruction;
import org.eclipse.lemminx.dom.DOMText;
import org.eclipse.lemminx.dom.DTDAttlistDecl;
import org.eclipse.lemminx.dom.DTDDeclNode;
import org.eclipse.lemminx.dom.DTDDeclParameter;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.settings.XMLFormattingOptions;
import org.eclipse.lemminx.utils.XMLBuilder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

/**
 * XML formatter support.
 *
 */
class XMLFormatter {

	private static final Logger LOGGER = Logger.getLogger(XMLFormatter.class.getName());
	private final XMLExtensionsRegistry extensionsRegistry;

	private static class XMLFormatterDocument {
		private final TextDocument textDocument;
		private final Range range;
		private final XMLFormattingOptions options;

		private int startOffset;
		private int endOffset;
		private DOMDocument fullDomDocument;
		private DOMDocument rangeDomDocument;
		private XMLBuilder xmlBuilder;
		private int indentLevel;

		/**
		 * XML formatter document.
		 */
		public XMLFormatterDocument(TextDocument textDocument, Range range, XMLFormattingOptions options) {
			this.textDocument = textDocument;
			this.range = range;
			this.options = options;
		}

		/**
		 * Returns a List containing a single TextEdit, containing the newly formatted changes
		 * of this.textDocument
		 * @return List containing a single TextEdit
		 * @throws BadLocationException
		 */
		public List<? extends TextEdit> format() throws BadLocationException {
			this.fullDomDocument = DOMParser.getInstance().parse(textDocument.getText(), textDocument.getUri(), null,
					false);

			if (range != null) {
				setupRangeFormatting(range);
			} else {
				setupFullFormatting(range);
			}

			this.indentLevel = getStartingIndentLevel();
			format(this.rangeDomDocument);

			List<? extends TextEdit> textEdits = getFormatTextEdit();
			return textEdits;
		}

		private void setupRangeFormatting(Range range) throws BadLocationException {
			int startOffset = this.textDocument.offsetAt(range.getStart());
			int endOffset = this.textDocument.offsetAt(range.getEnd());

			Position startPosition = this.textDocument.positionAt(startOffset);
			Position endPosition = this.textDocument.positionAt(endOffset);
			enlargePositionToGutters(startPosition, endPosition);

			this.startOffset = this.textDocument.offsetAt(startPosition);
			this.endOffset = this.textDocument.offsetAt(endPosition);

			String fullText = this.textDocument.getText();
			String rangeText = fullText.substring(this.startOffset, this.endOffset);

			this.rangeDomDocument = DOMParser.getInstance().parse(rangeText, this.textDocument.getUri(), null, false);

			if (containsTextWithinStartTag()) {
				adjustOffsetToStartTag();
				rangeText = fullText.substring(this.startOffset, this.endOffset);
				this.rangeDomDocument = DOMParser.getInstance().parse(rangeText, this.textDocument.getUri(), null, false);
			}	

			this.xmlBuilder = new XMLBuilder(this.options, "", textDocument.lineDelimiter(startPosition.getLine()));
		}

		private boolean containsTextWithinStartTag() {

			if (this.rangeDomDocument.getChildren().size() < 1) {
				return false;
			}

			DOMNode firstChild = this.rangeDomDocument.getChild(0);
			if (!firstChild.isText()) {
				return false;
			}

			int tagContentOffset = firstChild.getStart();
			int fullDocOffset = getFullOffsetFromRangeOffset(tagContentOffset);
			DOMNode fullNode = this.fullDomDocument.findNodeAt(fullDocOffset);

			if (!fullNode.isElement()) {
				return false;
			}
			return ((DOMElement) fullNode).isInStartTag(fullDocOffset);
		}

		private void adjustOffsetToStartTag() throws BadLocationException {
			int tagContentOffset = this.rangeDomDocument.getChild(0).getStart();
			int fullDocOffset = getFullOffsetFromRangeOffset(tagContentOffset);
			DOMNode fullNode = this.fullDomDocument.findNodeAt(fullDocOffset);
			Position nodePosition = this.textDocument.positionAt(fullNode.getStart());
			nodePosition.setCharacter(0);
			this.startOffset = this.textDocument.offsetAt(nodePosition);
		}

		private void setupFullFormatting(Range range) throws BadLocationException {
			this.startOffset = 0;
			this.endOffset = textDocument.getText().length();
			this.rangeDomDocument = this.fullDomDocument;

			Position startPosition = textDocument.positionAt(startOffset);
			this.xmlBuilder = new XMLBuilder(this.options, "", textDocument.lineDelimiter(startPosition.getLine()));
		}

		private void enlargePositionToGutters(Position start, Position end) throws BadLocationException {
			start.setCharacter(0);

			if (end.getCharacter() == 0 && end.getLine() > 0) {
				end.setLine(end.getLine() - 1);
			}

			end.setCharacter(this.textDocument.lineText(end.getLine()).length());
		}

		private int getStartingIndentLevel() throws BadLocationException {

			DOMNode startNode = this.fullDomDocument.findNodeAt(this.startOffset);

			if (startNode.isOwnerDocument()) {
				return 0;
			}

			DOMNode startNodeParent = startNode.getParentNode();

			if (startNodeParent.isOwnerDocument()) {
				return 0;
			}

			// the starting indent level is the parent's indent level + 1
			int startNodeIndentLevel = getNodeIndentLevel(startNodeParent) + 1;
			return startNodeIndentLevel;
		}

		private int getNodeIndentLevel(DOMNode node) throws BadLocationException {

			Position nodePosition = this.textDocument.positionAt(node.getStart());
			String textBeforeNode = this.textDocument.lineText(nodePosition.getLine()).substring(0,
					nodePosition.getCharacter() + 1);

			int spaceOrTab = getSpaceOrTabStartOfString(textBeforeNode);

			if (options.isInsertSpaces()) {
				return (spaceOrTab / this.options.getTabSize());
			}
			return spaceOrTab;
		}

		private int getSpaceOrTabStartOfString(String string) {
			int i = 0;
			int spaceOrTab = 0;
			while (i < string.length() && (string.charAt(i) == ' ' || string.charAt(i) == '\t')) {
				spaceOrTab++;
				i++;
			}
			return spaceOrTab;
		}

		private int getFullOffsetFromRangeOffset(int rangeOffset) {
			return rangeOffset + this.startOffset;
		}

		private DOMElement getFullDocElemFromRangeElem(DOMElement elemFromRangeDoc) {
			int fullOffset = -1;

			if (elemFromRangeDoc.hasStartTag()) {
				fullOffset = getFullOffsetFromRangeOffset(elemFromRangeDoc.getStartTagOpenOffset()) + 1; 
				// +1 because offset must be here: <|root
				// for DOMNode.findNodeAt() to find the correct element
			} else if (elemFromRangeDoc.hasEndTag()) {
				fullOffset = getFullOffsetFromRangeOffset(elemFromRangeDoc.getEndTagCloseOffset()) - 1;
				// -1 because offset must be here: root|>
				// for DOMNode.findNodeAt() to find the correct element
			} else {
				return null;
			}

			DOMElement elemFromFullDoc = (DOMElement) this.fullDomDocument.findNodeAt(fullOffset);
			return elemFromFullDoc;
		}

		private boolean startTagExistsInRangeDocument(DOMNode node) {
			if (!node.isElement()) {
				return false;
			}

			return ((DOMElement) node).hasStartTag();
		}

		private boolean startTagExistsInFullDocument(DOMNode node) {
			if (!node.isElement()) {
				return false;
			}

			DOMElement elemFromFullDoc = getFullDocElemFromRangeElem((DOMElement) node);

			if (elemFromFullDoc == null) {
				return false;
			}

			return elemFromFullDoc.hasStartTag();
		}

		private void format(DOMNode node) throws BadLocationException {

			if (node.getNodeType() != DOMNode.DOCUMENT_NODE) {
				boolean doLineFeed;
				if (node.getOwnerDocument().isDTD()) {
					doLineFeed = false;
				} else {
					doLineFeed = !(node.isComment() && ((DOMComment) node).isCommentSameLineEndTag())
							&& (!node.isText() || (!((DOMText) node).isWhitespace() && ((DOMText) node).hasSiblings()));
				}

				if (this.indentLevel > 0 && doLineFeed) {
					// add new line + indent
					if (!node.isChildOfOwnerDocument() || node.getPreviousNonTextSibling() != null) {
						this.xmlBuilder.linefeed();
					}

					if (!startTagExistsInRangeDocument(node) && startTagExistsInFullDocument(node)) {
						DOMNode startNode = getFullDocElemFromRangeElem((DOMElement) node);
						int currentIndentLevel = getNodeIndentLevel(startNode);
						this.xmlBuilder.indent(currentIndentLevel);
						this.indentLevel = currentIndentLevel;
					} else {
						this.xmlBuilder.indent(this.indentLevel);
					}
				}
				// generate start element
				if (node.isElement()) {
					DOMElement element = (DOMElement) node;
					String tag = element.getTagName();
					if (element.hasEndTag() && !element.hasStartTag()) {
						// bad element which have not start tag (ex: <\root>)
						xmlBuilder.endElement(tag, element.isEndTagClosed());
					} else {
						xmlBuilder.startElement(tag, false);
						if (element.hasAttributes()) {
							// generate attributes
							List<DOMAttr> attributes = element.getAttributeNodes();
							if (hasSingleAttributeInFullDoc(element)) {
								DOMAttr singleAttribute = attributes.get(0);
								xmlBuilder.addSingleAttribute(singleAttribute.getName(),
										singleAttribute.getOriginalValue());
							} else {
								for (DOMAttr attr : attributes) {
									xmlBuilder.addAttribute(attr, this.indentLevel);
								}
							}
						}

						if (element.isStartTagClosed()) {
							xmlBuilder.closeStartElement();
						}

						boolean hasElements = false;
						if (node.hasChildNodes()) {
							// element has body
							
							this.indentLevel++;
							for (DOMNode child : node.getChildren()) {
								boolean textElement = !child.isText();

								hasElements = hasElements | textElement;

								format(child);
							}
							this.indentLevel--;
						}
						if (element.hasEndTag()) {
							if (hasElements) {
								this.xmlBuilder.linefeed();
								this.xmlBuilder.indent(this.indentLevel);
							}
							// end tag element is done, only if the element is closed
							// the format, doesn't fix the close tag
							if (element.hasEndTag() && element.getEndTagOpenOffset() <= this.endOffset) {
								this.xmlBuilder.endElement(tag, element.isEndTagClosed());
							} else {
								this.xmlBuilder.selfCloseElement();
							}
						} else if (element.isSelfClosed()) {
							this.xmlBuilder.selfCloseElement();

						}
					}
					return;

				} else if (node.isCDATA()) {
					DOMCDATASection cdata = (DOMCDATASection) node;
					this.xmlBuilder.startCDATA();
					this.xmlBuilder.addContentCDATA(cdata.getData());
					this.xmlBuilder.endCDATA();
				} else if (node.isComment()) {
					DOMComment comment = (DOMComment) node;
					this.xmlBuilder.startComment(comment);
					this.xmlBuilder.addContentComment(comment.getData());
					this.xmlBuilder.endComment();
					if (this.indentLevel == 0) {
						this.xmlBuilder.linefeed();
					}
				} else if (node.isProcessingInstruction()) {
					addPIToXMLBuilder(node, this.xmlBuilder);
					if (this.indentLevel == 0) {
						this.xmlBuilder.linefeed();
					}
				} else if (node.isProlog()) {
					addPrologToXMLBuilder(node, this.xmlBuilder);
					this.xmlBuilder.linefeed();
				} else if (node.isText()) {
					DOMText textNode = (DOMText) node;

					// Generate content
					String content = textNode.getData();
					xmlBuilder.addContent(content, textNode.isWhitespace(), textNode.hasSiblings(),
							textNode.getDelimiter(), this.indentLevel);
					return;
				} else if (node.isDoctype()) {
					boolean isDTD = node.getOwnerDocument().isDTD();
					DOMDocumentType documentType = (DOMDocumentType) node;
					if (!isDTD) {
						this.xmlBuilder.startDoctype();
						List<DTDDeclParameter> params = documentType.getParameters();

						for (DTDDeclParameter param : params) {
							if (!documentType.isInternalSubset(param)) {
								xmlBuilder.addParameter(param.getParameter());
							} else {
								xmlBuilder.startDoctypeInternalSubset();
								xmlBuilder.linefeed();
								// level + 1 since the 'level' value is the doctype tag's level
								formatDTD(documentType, this.indentLevel + 1, this.endOffset, this.xmlBuilder);
								xmlBuilder.linefeed();
								xmlBuilder.endDoctypeInternalSubset();
							}
						}
						if (documentType.isClosed()) {
							xmlBuilder.endDoctype();
						}
						xmlBuilder.linefeed();
					} else {
						formatDTD(documentType, 0, this.endOffset, this.xmlBuilder);
					}
					return;
				}
			} else if (node.hasChildNodes()) {
				// Other nodes kind like root
				for (DOMNode child : node.getChildren()) {
					format(child);
				}
			}
		}

		private static boolean formatDTD(DOMDocumentType doctype, int level, int end, XMLBuilder xmlBuilder) {
			DOMNode previous = null;
			for (DOMNode node : doctype.getChildren()) {
				if (previous != null) {
					xmlBuilder.linefeed();
				}

				xmlBuilder.indent(level);

				if (node.isText()) {
					xmlBuilder.addContent(((DOMText) node).getData().trim());
				} else if (node.isComment()) {
					DOMComment comment = (DOMComment) node;
					xmlBuilder.startComment(comment);
					xmlBuilder.addContentComment(comment.getData());
					xmlBuilder.endComment();
				} else if (node.isProcessingInstruction()) {
					addPIToXMLBuilder(node, xmlBuilder);
				} else if (node.isProlog()) {
					addPrologToXMLBuilder(node, xmlBuilder);
				} else {
					boolean setEndBracketOnNewLine = false;
					DTDDeclNode decl = (DTDDeclNode) node;
					xmlBuilder.addDeclTagStart(decl);

					if (decl.isDTDAttListDecl()) {
						DTDAttlistDecl attlist = (DTDAttlistDecl) decl;
						List<DTDAttlistDecl> internalDecls = attlist.getInternalChildren();

						if (internalDecls == null) {
							for (DTDDeclParameter param : decl.getParameters()) {
								xmlBuilder.addParameter(param.getParameter());
							}
						} else {
							boolean multipleInternalAttlistDecls = false;
							List<DTDDeclParameter> params = attlist.getParameters();
							DTDDeclParameter param;
							for (int i = 0; i < params.size(); i++) {
								param = params.get(i);
								if (attlist.getNameParameter().equals(param)) {
									xmlBuilder.addParameter(param.getParameter());
									if (attlist.getParameters().size() > 1) { // has parameters after elementName
										xmlBuilder.linefeed();
										xmlBuilder.indent(level + 1);
										setEndBracketOnNewLine = true;
										multipleInternalAttlistDecls = true;
									}
								} else {
									if (multipleInternalAttlistDecls && i == 1) {
										xmlBuilder.addUnindentedParameter(param.getParameter());
									} else {
										xmlBuilder.addParameter(param.getParameter());
									}
								}
							}

							for (DTDAttlistDecl attlistDecl : internalDecls) {
								xmlBuilder.linefeed();
								xmlBuilder.indent(level + 1);
								params = attlistDecl.getParameters();
								for (int i = 0; i < params.size(); i++) {
									param = params.get(i);

									if (i == 0) {
										xmlBuilder.addUnindentedParameter(param.getParameter());
									} else {
										xmlBuilder.addParameter(param.getParameter());
									}
								}
							}
						}
					} else {
						for (DTDDeclParameter param : decl.getParameters()) {
							xmlBuilder.addParameter(param.getParameter());
						}
					}
					if (setEndBracketOnNewLine) {
						xmlBuilder.linefeed();
						xmlBuilder.indent(level);
					}
					if (decl.isClosed()) {
						xmlBuilder.closeStartElement();
					}
				}
				previous = node;
			}
			return true;
		}

		private boolean hasSingleAttributeInFullDoc(DOMElement element) {
			DOMElement fullElement = getFullDocElemFromRangeElem(element);
			return fullElement.getAttributeNodes().size() == 1;
		}

		private List<? extends TextEdit> getFormatTextEdit() throws BadLocationException {
			Position startPosition = this.textDocument.positionAt(this.startOffset);
			Position endPosition = this.textDocument.positionAt(this.endOffset);
			Range r = new Range(startPosition, endPosition);
			List<TextEdit> edits = new ArrayList<>();
			edits.add(new TextEdit(r, this.xmlBuilder.toString()));
			return edits;
		}

		private static boolean isFirstChildNode(DOMNode node) {
			return node.equals(node.getParentNode().getFirstChild());
		}

		private static boolean isPreviousSiblingNodeType(DOMNode node, short nodeType) {
			DOMNode previousNode = node.getPreviousSibling();
			return previousNode != null && previousNode.getNodeType() == nodeType;
		}

		private static void addPIToXMLBuilder(DOMNode node, XMLBuilder xml) {
			DOMProcessingInstruction processingInstruction = (DOMProcessingInstruction) node;
			xml.startPrologOrPI(processingInstruction.getTarget());

			String content = processingInstruction.getData();
			if (content.length() > 0) {
				xml.addContentPI(content);
			} else {
				xml.addContent(" ");
			}

			xml.endPrologOrPI();
		}

		private static void addPrologToXMLBuilder(DOMNode node, XMLBuilder xml) {
			DOMProcessingInstruction processingInstruction = (DOMProcessingInstruction) node;
			xml.startPrologOrPI(processingInstruction.getTarget());
			if (node.hasAttributes()) {
				addAttributes(node, xml);
			}
			xml.endPrologOrPI();
		}

		/**
		 * Will add all attributes, to the given builder, on a single line
		 */
		private static void addAttributes(DOMNode node, XMLBuilder xmlBuilder) {
			List<DOMAttr> attrs = node.getAttributeNodes();
			if (attrs == null) {
				return;
			}
			for (DOMAttr attr : attrs) {
				xmlBuilder.addAttributesOnSingleLine(attr, true);
			}
			xmlBuilder.appendSpace();
		}
	}

	public XMLFormatter(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	/**
	 * Returns a List containing a single TextEdit, containing the newly formatted changes
	 * of the document.
	 * @param textDocument document to perform formatting on
	 * @param range specified range in which formatting will be done
	 * @return List containing a TextEdit with formatting changes
	 */
	public List<? extends TextEdit> format(TextDocument textDocument, Range range,
			XMLFormattingOptions formattingOptions) {
		try {
			XMLFormatterDocument formatterDocument = new XMLFormatterDocument(textDocument, range, formattingOptions);
			return formatterDocument.format();
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Formatting failed due to BadLocation", e);
		}
		return null;
	}
}
