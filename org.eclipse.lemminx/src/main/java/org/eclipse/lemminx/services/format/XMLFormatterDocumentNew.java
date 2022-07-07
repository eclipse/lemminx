/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMProcessingInstruction;
import org.eclipse.lemminx.dom.DOMText;
import org.eclipse.lemminx.services.extensions.format.IFormatterParticipant;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Experimental XML formatter which generates several text edit to remove, add,
 * update spaces / indent.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLFormatterDocumentNew {

	private static final Logger LOGGER = Logger.getLogger(XMLFormatterDocumentNew.class.getName());

	private static final String XML_SPACE_ATTR = "xml:space";

	private static final String XML_SPACE_ATTR_DEFAULT = "default";

	private static final String XML_SPACE_ATTR_PRESERVE = "preserve";

	private final DOMDocument xmlDocument;
	private final TextDocument textDocument;
	private final String lineDelimiter;
	private final SharedSettings sharedSettings;

	private final DOMProcessingInstructionFormatter processingInstructionFormatter;

	private final DOMDocTypeFormatter docTypeFormatter;

	private final DOMElementFormatter elementFormatter;

	private final DOMAttributeFormatter attributeFormatter;

	private final DOMTextFormatter textFormatter;

	private final Collection<IFormatterParticipant> formatterParticipants;

	private int startOffset = -1;
	private int endOffset = -1;

	private CancelChecker cancelChecker;

	/**
	 * XML formatter document.
	 */
	public XMLFormatterDocumentNew(DOMDocument xmlDocument, Range range, SharedSettings sharedSettings,
			Collection<IFormatterParticipant> formatterParticipants) {
		this.xmlDocument = xmlDocument;
		this.textDocument = xmlDocument.getTextDocument();
		this.lineDelimiter = computeLineDelimiter(textDocument);
		if (range != null) {
			try {
				startOffset = textDocument.offsetAt(range.getStart());
				endOffset = textDocument.offsetAt(range.getEnd());
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		this.sharedSettings = sharedSettings;
		this.formatterParticipants = formatterParticipants;
		this.docTypeFormatter = new DOMDocTypeFormatter(this);
		this.attributeFormatter = new DOMAttributeFormatter(this);
		this.elementFormatter = new DOMElementFormatter(this, attributeFormatter);
		this.processingInstructionFormatter = new DOMProcessingInstructionFormatter(this, attributeFormatter);
		this.textFormatter = new DOMTextFormatter(this);

	}

	private static String computeLineDelimiter(TextDocument textDocument) {
		try {
			return textDocument.lineDelimiter(0);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return System.lineSeparator();
	}

	/**
	 * Returns a List containing multiple TextEdit, containing the newly formatted
	 * changes of an XML document.
	 * 
	 * @return List containing multiple TextEdit of an XML document.
	 * 
	 * @throws BadLocationException
	 */
	public List<? extends TextEdit> format() throws BadLocationException {
		return format(xmlDocument, startOffset, endOffset);
	}

	public List<? extends TextEdit> format(DOMDocument document, int start, int end) {
		List<TextEdit> edits = new ArrayList<>();

		// get initial document region
		DOMNode currentDOMNode = getDOMNodeToFormat(document, start, end);

		if (currentDOMNode != null) {
			int startOffset = currentDOMNode.getStart();

			XMLFormattingConstraints parentConstraints = getNodeConstraints(currentDOMNode);

			// initialize available line width
			int lineWidth = getMaxLineWidth();

			try {
				int lineOffset = textDocument.lineOffsetAt(startOffset);
				lineWidth = lineWidth - (startOffset - lineOffset);
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			parentConstraints.setAvailableLineWidth(lineWidth);

			// format all siblings (and their children) as long they
			// overlap with start/end offset
			if (currentDOMNode.isElement()) {
				parentConstraints.setFormatElementCategory(getFormatElementCategory((DOMElement) currentDOMNode, null));
			} else {
				parentConstraints.setFormatElementCategory(FormatElementCategory.IgnoreSpace);
			}
			formatSiblings(edits, currentDOMNode, parentConstraints, start, end);
		}

		boolean insertFinalNewline = isInsertFinalNewline();
		if (isTrimFinalNewlines()) {
			trimFinalNewlines(insertFinalNewline, edits);
		}
		if (insertFinalNewline) {
			String xml = textDocument.getText();
			int endDocument = xml.length() - 1;
			if (endDocument >= 0) {
				char c = xml.charAt(endDocument);
				if (c != '\n') {
					try {
						Position pos = textDocument.positionAt(endDocument);
						pos.setCharacter(pos.getCharacter() + 1);
						Range range = new Range(pos, pos);
						edits.add(new TextEdit(range, lineDelimiter));
					} catch (BadLocationException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		}
		return edits;
	}

	/**
	 * Returns the DOM node to format according to the given range and the DOM
	 * document otherwise.
	 * 
	 * @param document the DOM document.
	 * @param start    the start range offset and -1 otherwise.
	 * @param end      the end range offset and -1 otherwise.
	 * 
	 * @return the DOM node to format according to the given range and the DOM
	 *         document otherwise.
	 */
	private static DOMNode getDOMNodeToFormat(DOMDocument document, int start, int end) {
		if (start != -1 && end != -1) {
			DOMNode startNode = document.findNodeAt(start);
			DOMNode endNode = document.findNodeBefore(end);

			if (endNode.getStart() == start) {
				// ex :
				// <div>
				// |<img />|
				// </div>
				return endNode;
			}

			if (isCoverNode(startNode, endNode)) {
				return startNode;
			} else if (isCoverNode(endNode, startNode)) {
				return endNode;
			} else {
				DOMNode startParent = startNode.getParentNode();
				DOMNode endParent = endNode.getParentNode();
				while (startParent != null && endParent != null) {
					if (isCoverNode(startParent, endParent)) {
						return startParent;
					} else if (isCoverNode(endParent, startParent)) {
						return endParent;
					}
					startParent = startParent.getParentNode();
					endParent = endParent.getParentNode();
				}
			}
		}
		return document;
	}

	private static boolean isCoverNode(DOMNode startNode, DOMNode endNode) {
		return (startNode.getStart() < endNode.getStart() && startNode.getEnd() > endNode.getEnd())
				|| startNode == endNode;
	}

	/**
	 * Returns the DOM node constraints of the given DOM node.
	 * 
	 * @param node the DOM node.
	 * 
	 * @return the DOM node constraints of the given DOM node.
	 */
	private XMLFormattingConstraints getNodeConstraints(DOMNode node) {
		XMLFormattingConstraints result = new XMLFormattingConstraints();
		// Compute the indent level according to the parent node.
		int indentLevel = 0;
		while (node != null) {
			node = node.getParentElement();
			if (node != null) {
				indentLevel++;
			}
		}
		result.setIndentLevel(indentLevel);
		return result;
	}

	private void formatSiblings(List<TextEdit> edits, DOMNode domNode, XMLFormattingConstraints parentConstraints,
			int start, int end) {
		DOMNode currentDOMNode = domNode;
		while (currentDOMNode != null) {
			if (cancelChecker != null) {
				cancelChecker.checkCanceled();
			}
			format(currentDOMNode, parentConstraints, start, end, edits);
			currentDOMNode = currentDOMNode.getNextSibling();
		}
	}

	private void format(DOMNode child, XMLFormattingConstraints parentConstraints, int start, int end,
			List<TextEdit> edits) {

		switch (child.getNodeType()) {

		case Node.DOCUMENT_TYPE_NODE:
			DOMDocumentType docType = (DOMDocumentType) child;
			docTypeFormatter.formatDocType(docType, parentConstraints, start, end, edits);
			break;

		case Node.DOCUMENT_NODE:
			DOMDocument document = (DOMDocument) child;
			formatChildren(document, parentConstraints, start, end, edits);
			break;

		case DOMNode.PROCESSING_INSTRUCTION_NODE:
			DOMProcessingInstruction processingInstruction = (DOMProcessingInstruction) child;
			processingInstructionFormatter.formatProcessingInstruction(processingInstruction, parentConstraints, edits);
			break;

		case Node.ELEMENT_NODE:
			DOMElement element = (DOMElement) child;
			elementFormatter.formatElement(element, parentConstraints, start, end, edits);
			break;

		case Node.TEXT_NODE:
			DOMText textNode = (DOMText) child;
			textFormatter.formatText(textNode, parentConstraints, edits);
			break;

		default:
			// unknown, so just leave alone for now but make sure to update
			// available line width
			int width = updateLineWidthWithLastLine(child, parentConstraints.getAvailableLineWidth());
			parentConstraints.setAvailableLineWidth(width);
		}
	}

	public void formatChildren(DOMNode currentDOMNode, XMLFormattingConstraints parentConstraints, int start, int end,
			List<TextEdit> edits) {
		for (DOMNode child : currentDOMNode.getChildren()) {
			format(child, parentConstraints, start, end, edits);
		}
	}

	void removeLeftSpaces(int to, List<TextEdit> edits) {
		replaceLeftSpacesWith(to, "", edits);
	}

	void removeLeftSpaces(int from, int to, List<TextEdit> edits) {
		replaceLeftSpacesWith(from, to, "", edits);
	}

	void replaceLeftSpacesWithOneSpace(int to, List<TextEdit> edits) {
		replaceLeftSpacesWith(to, " ", edits);
	}

	void replaceLeftSpacesWithOneSpace(int from, int to, List<TextEdit> edits) {
		replaceLeftSpacesWith(from, to, " ", edits);
	}

	void replaceLeftSpacesWith(int to, String replacement, List<TextEdit> edits) {
		replaceLeftSpacesWith(-1, to, replacement, edits);
	}

	void replaceLeftSpacesWith(int leftLimit, int to, String replacement, List<TextEdit> edits) {
		int from = getLeftWhitespacesOffset(leftLimit, to);
		createTextEditIfNeeded(from, to, replacement, edits);
	}

	void replaceQuoteWithPreferred(int from, int to, String replacement, List<TextEdit> edits){
		createTextEditIfNeeded(from, to, replacement, edits);
	}

	private int getLeftWhitespacesOffset(int leftLimit, int to) {
		String text = textDocument.getText();
		int from = leftLimit != -1 ? leftLimit : to - 1;
		int limit = leftLimit != -1 ? leftLimit : 0;
		for (int i = to - 1; i >= limit; i--) {
			char c = text.charAt(i);
			if (!Character.isWhitespace(c)) {
				from = i;
				break;
			}
		}
		return from;
	}

	int replaceLeftSpacesWithIndentation(int indentLevel, int offset, boolean addLineSeparator, List<TextEdit> edits) {
		int start = offset - 1;
		if (start > 0) {
			String expectedSpaces = getIndentSpaces(indentLevel, addLineSeparator);
			createTextEditIfNeeded(start, offset, expectedSpaces, edits);
			return expectedSpaces.length();
		}
		return 0;
	}

	boolean hasLineBreak(int startAttr, int start) {
		String text = textDocument.getText();
		for (int i = startAttr; i < start; i++) {
			char c = text.charAt(i);
			if (isLineSeparator(c)) {
				return true;
			}
		}
		return false;
	}

	// DTD formatting

	// ------- Utilities method

	int updateLineWidthWithLastLine(DOMNode child, int availableLineWidth) {
		String text = textDocument.getText();
		int lineWidth = availableLineWidth;
		int end = child.getEnd();
		// Check if next char after the end of the DOM node is a new line feed.
		if (end < text.length()) {
			char c = text.charAt(end);
			if (isLineSeparator(c)) {
				// ex: <?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n
				return getMaxLineWidth();
			}
		}
		for (int i = end - 1; i > child.getStart(); i--) {
			char c = text.charAt(i);
			if (isLineSeparator(c)) {
				return lineWidth;
			} else {
				lineWidth--;
			}
		}
		return lineWidth;
	}

	private static boolean isLineSeparator(char c) {
		return c == '\r' || c == '\n';
	}

	void insertLineBreak(int start, int end, List<TextEdit> edits) {
		createTextEditIfNeeded(start, end, lineDelimiter, edits);
	}

	void replaceSpacesWithOneSpace(int spaceStart, int spaceEnd, List<TextEdit> edits) {
		if (spaceStart >= 0) {
			spaceEnd = spaceEnd == -1 ? spaceStart + 1 : spaceEnd + 1;
			// Replace several spaces with one space
			// <foo>a[space][space][space]b</foo>
			// --> <foo>a[space]b</foo>
			replaceLeftSpacesWithOneSpace(spaceStart, spaceEnd, edits);
		}
	}

	/**
	 * Returns the format element category of the given DOM element.
	 * 
	 * @param element           the DOM element.
	 * @param parentConstraints the parent constraints.
	 * 
	 * @return the format element category of the given DOM element.
	 */
	public FormatElementCategory getFormatElementCategory(DOMElement element,
			XMLFormattingConstraints parentConstraints) {
		if (!element.isClosed()) {
			return parentConstraints.getFormatElementCategory();
		}

		// Get the category from the settings
		FormatElementCategory fromSettings = sharedSettings.getFormattingSettings().getFormatElementCategory(element);
		if (fromSettings != null) {
			return fromSettings;
		}

		// Get the category from the participants (ex : from the XSD/DTD grammar
		// information)
		for (IFormatterParticipant participant : formatterParticipants) {
			FormatElementCategory fromParticipant = participant.getFormatElementCategory(element, parentConstraints,
					sharedSettings);
			if (fromParticipant != null) {
				return fromParticipant;
			}
		}

		if (XML_SPACE_ATTR_PRESERVE.equals(element.getAttribute(XML_SPACE_ATTR))) {
			return FormatElementCategory.PreserveSpace;
		}

		if (parentConstraints != null) {
			if (parentConstraints.getFormatElementCategory() == FormatElementCategory.PreserveSpace) {
				if (!XML_SPACE_ATTR_DEFAULT.equals(element.getAttribute(XML_SPACE_ATTR))) {
					return FormatElementCategory.PreserveSpace;
				}
			}
		}

		boolean hasElement = false;
		boolean hasText = false;
		boolean onlySpaces = true;
		for (DOMNode child : element.getChildren()) {
			if (child.isElement()) {
				hasElement = true;
			} else if (child.isText()) {
				onlySpaces = ((Text) child).isElementContentWhitespace();
				if (!onlySpaces) {
					hasText = true;
				}
			}
			if (hasElement && hasText) {
				return FormatElementCategory.MixedContent;
			}
		}
		if (hasElement && onlySpaces) {
			return FormatElementCategory.IgnoreSpace;
		}
		return FormatElementCategory.NormalizeSpace;
	}

	void createTextEditIfNeeded(int from, int to, String expectedContent, List<TextEdit> edits) {
		TextEdit edit = TextEditUtils.createTextEditIfNeeded(from, to, expectedContent, textDocument);
		if (edit != null) {
			edits.add(edit);
		}
	}

	private String getIndentSpaces(int level, boolean addLineSeparator) {
		StringBuilder spaces = new StringBuilder();
		if (addLineSeparator) {
			spaces.append(lineDelimiter);
		}

		for (int i = 0; i < level; i++) {
			if (isInsertSpaces()) {
				for (int j = 0; j < getTabSize(); j++) {
					spaces.append(" ");
				}
			} else {
				spaces.append("\t");
			}
		}
		return spaces.toString();
	}

	private void trimFinalNewlines(boolean insertFinalNewline, List<TextEdit> edits) {
		String xml = textDocument.getText();
		int end = xml.length() - 1;
		int i = end;
		while (i >= 0 && isLineSeparator(xml.charAt(i))) {
			i--;
		}
		if (end > i) {
			if (insertFinalNewline) {
				// re-adjust offset to keep insert final new line
				i++;
				if (xml.charAt(end - 1) == '\r') {
					i++;
				}
			}
			if (end > i) {
				try {
					Position endPos = textDocument.positionAt(end + 1);
					Position startPos = textDocument.positionAt(i + 1);
					Range range = new Range(startPos, endPos);
					edits.add(new TextEdit(range, ""));
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	int getMaxLineWidth() {
		return sharedSettings.getFormattingSettings().getMaxLineWidth();
	}

	private int getTabSize() {
		return sharedSettings.getFormattingSettings().getTabSize();
	}

	private boolean isInsertSpaces() {
		return sharedSettings.getFormattingSettings().isInsertSpaces();
	}

	private boolean isTrimFinalNewlines() {
		return sharedSettings.getFormattingSettings().isTrimFinalNewlines();
	}

	private boolean isInsertFinalNewline() {
		return sharedSettings.getFormattingSettings().isInsertFinalNewline();
	}

	SharedSettings getSharedSettings() {
		return sharedSettings;
	}

	String getLineDelimiter() {
		return lineDelimiter;
	}

	String getText() {
		return textDocument.getText();
	}
}