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
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.XMLFormattingOptions.EmptyElements;
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
		private final SharedSettings sharedSettings;
		private final EmptyElements emptyElements;

		private int startOffset;
		private int endOffset;
		private DOMDocument fullDomDocument;
		private DOMDocument rangeDomDocument;
		private XMLBuilder xmlBuilder;
		private int indentLevel;
		private boolean linefeedOnNextWrite;

		/**
		 * XML formatter document.
		 */
		public XMLFormatterDocument(TextDocument textDocument, Range range, SharedSettings sharedSettings) {
			this.textDocument = textDocument;
			this.range = range;
			this.sharedSettings = sharedSettings;
			this.emptyElements = sharedSettings.getFormattingSettings().getEmptyElements();
			this.linefeedOnNextWrite = false;
		}

		/**
		 * Returns a List containing a single TextEdit, containing the newly formatted
		 * changes of this.textDocument
		 * 
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
				this.rangeDomDocument = DOMParser.getInstance().parse(rangeText, this.textDocument.getUri(), null,
						false);
			}

			this.xmlBuilder = new XMLBuilder(this.sharedSettings, "",
					textDocument.lineDelimiter(startPosition.getLine()));
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
			this.xmlBuilder = new XMLBuilder(this.sharedSettings,
					"", textDocument.lineDelimiter(startPosition.getLine()));
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

			if (this.sharedSettings.getFormattingSettings().isInsertSpaces()) {
				return (spaceOrTab / this.sharedSettings.getFormattingSettings().getTabSize());
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

			if (linefeedOnNextWrite && (!node.isText() || !((DOMText) node).isWhitespace())) {
				this.xmlBuilder.linefeed();
				linefeedOnNextWrite = false;
			}

			if (node.getNodeType() != DOMNode.DOCUMENT_NODE) {
				boolean doLineFeed = !node.getOwnerDocument().isDTD() &&
						!(node.isComment() && ((DOMComment) node).isCommentSameLineEndTag()) &&
						(!node.isText() || (!((DOMText) node).isWhitespace() && ((DOMText) node).hasSiblings()));

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
				if (node.isElement()) {
					// Format Element
					formatElement((DOMElement) node);
				} else if (node.isCDATA()) {
					// Format CDATA
					formatCDATA((DOMCDATASection) node);
				} else if (node.isComment()) {
					// Format comment
					formatComment((DOMComment) node);
				} else if (node.isProcessingInstruction()) {
					// Format processing instruction
					formatProcessingInstruction(node);
				} else if (node.isProlog()) {
					// Format prolog
					formatProlog(node);
				} else if (node.isText()) {
					// Format Text
					formatText((DOMText) node);
				} else if (node.isDoctype()) {
					// Format document type
					formatDocumentType((DOMDocumentType) node);
				}
			} else if (node.hasChildNodes()) {
				// Other nodes kind like root
				for (DOMNode child : node.getChildren()) {
					format(child);
				}
			}
		}

		/**
		 * Format the given DOM prolog
		 *
		 * @param node the DOM prolog to format.
		 */
		private void formatProlog(DOMNode node) {
			addPrologToXMLBuilder(node, this.xmlBuilder);
			linefeedOnNextWrite = true;
		}

		/**
		 * Format the given DOM text node.
		 * 
		 * @param textNode the DOM text node to format.
		 */
		private void formatText(DOMText textNode) {
			String content = textNode.getData();
			if (textNode.equals(this.fullDomDocument.getLastChild())) {
				xmlBuilder.addContent(content);
			} else {
				xmlBuilder.addContent(content, textNode.isWhitespace(), textNode.hasSiblings(),
						textNode.getDelimiter());
			}
		}

		/**
		 * Format the given DOM document type.
		 * 
		 * @param documentType the DOM document type to format.
		 */
		private void formatDocumentType(DOMDocumentType documentType) {
			boolean isDTD = documentType.getOwnerDocument().isDTD();
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
				linefeedOnNextWrite = true;

			} else {
				formatDTD(documentType, 0, this.endOffset, this.xmlBuilder);
			}
		}

		/**
		 * Format the given DOM ProcessingIntsruction.
		 * 
		 * @param element the DOM ProcessingIntsruction to format.
		 *
		 */
		private void formatProcessingInstruction(DOMNode node) {
			addPIToXMLBuilder(node, this.xmlBuilder);
			if (this.indentLevel == 0) {
				this.xmlBuilder.linefeed();
			}
		}

		/**
		 * Format the given DOM Comment
		 * 
		 * @param element the DOM Comment to format.
		 *
		 */
		private void formatComment(DOMComment comment) {
			this.xmlBuilder.startComment(comment);
			this.xmlBuilder.addContentComment(comment.getData());
			this.xmlBuilder.endComment();
			if (this.indentLevel == 0) {
				linefeedOnNextWrite = true;
			}
		}

		/**
		 * Format the given DOM CDATA
		 * 
		 * @param element the DOM CDATA to format.
		 * 
		 */
		private void formatCDATA(DOMCDATASection cdata) {
			this.xmlBuilder.startCDATA();
			this.xmlBuilder.addContentCDATA(cdata.getData());
			this.xmlBuilder.endCDATA();
		}

		/**
		 * Format the given DOM element
		 * 
		 * @param element the DOM element to format.
		 * 
		 * @throws BadLocationException
		 */
		private void formatElement(DOMElement element) throws BadLocationException {
			String tag = element.getTagName();
			if (element.hasEndTag() && !element.hasStartTag()) {
				// bad element without start tag (ex: <\root>)
				xmlBuilder.endElement(tag, element.isEndTagClosed());
			} else {
				// generate start element
				xmlBuilder.startElement(tag, false);
				if (element.hasAttributes()) {
					// generate attributes
					List<DOMAttr> attributes = element.getAttributeNodes();
					if (hasSingleAttributeInFullDoc(element)) {
						DOMAttr singleAttribute = attributes.get(0);
						xmlBuilder.addSingleAttribute(singleAttribute.getName(), singleAttribute.getOriginalValue());
					} else {
						for (DOMAttr attr : attributes) {
							xmlBuilder.addAttribute(attr, this.indentLevel);
						}
					}
				}

				EmptyElements emptyElements = getEmptyElements(element);
				switch (emptyElements) {
				case expand:
					// expand empty element: <example /> -> <example></example>
					xmlBuilder.closeStartElement();
					// end tag element is done, only if the element is closed
					// the format, doesn't fix the close tag
					this.xmlBuilder.endElement(tag, true);
					break;
				case collapse:
					// collapse empty element: <example></example> -> <example />
					this.xmlBuilder.selfCloseElement();
					break;
				default:
					if (element.isStartTagClosed()) {
						xmlBuilder.closeStartElement();
					}
					boolean hasElements = false;
					if (element.hasChildNodes()) {
						// element has body

						this.indentLevel++;
						for (DOMNode child : element.getChildren()) {
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
			}
		}

		/**
		 * Return the option to use to generate empty elements.
		 * 
		 * @param element the DOM element
		 * @return the option to use to generate empty elements.
		 */
		private EmptyElements getEmptyElements(DOMElement element) {
			if (this.emptyElements != EmptyElements.ignore) {
				if (element.isClosed() && element.isEmpty()) {
					// Element is empty and closed
					switch (this.emptyElements) {
					case expand:
					case collapse: {
						if (this.sharedSettings.getFormattingSettings().isPreserveEmptyContent()) {
							// preserve content
							if (element.hasChildNodes()) {
								// The element is empty and contains somes spaces which must be preserved
								return EmptyElements.ignore;
							}
						}
						return this.emptyElements;
					}
					default:
						return this.emptyElements;
					}
				}
			}
			return EmptyElements.ignore;
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

			// check if format range reaches the end of the document
			if (this.endOffset == this.textDocument.getText().length()) {

				if (this.sharedSettings.getFormattingSettings().isTrimFinalNewlines()) {
					this.xmlBuilder.trimFinalNewlines();
				}

				if (this.sharedSettings.getFormattingSettings().isInsertFinalNewline() && !this.xmlBuilder.isLastLineEmptyOrWhitespace()) {
					this.xmlBuilder.linefeed();
				}
			}

			edits.add(new TextEdit(r, this.xmlBuilder.toString()));
			return edits;
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
				addPrologAttributes(node, xml);
			}
			xml.endPrologOrPI();
		}

		/**
		 * Will add all attributes, to the given builder, on a single line
		 */
		private static void addPrologAttributes(DOMNode node, XMLBuilder xmlBuilder) {
			List<DOMAttr> attrs = node.getAttributeNodes();
			if (attrs == null) {
				return;
			}
			for (DOMAttr attr : attrs) {
				xmlBuilder.addPrologAttribute(attr);
			}
		}
	}

	public XMLFormatter(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	/**
	 * Returns a List containing a single TextEdit, containing the newly formatted
	 * changes of the document.
	 * 
	 * @param textDocument   document to perform formatting on
	 * @param range          specified range in which formatting will be done
	 * @param sharedSettings settings containing formatting preferences
	 * @return List containing a TextEdit with formatting changes
	 */
	public List<? extends TextEdit> format(TextDocument textDocument, Range range,
			SharedSettings sharedSettings) {
		try {
			XMLFormatterDocument formatterDocument = new XMLFormatterDocument(textDocument, range,
					sharedSettings);
			return formatterDocument.format();
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Formatting failed due to BadLocation", e);
		}
		return null;
	}
}
