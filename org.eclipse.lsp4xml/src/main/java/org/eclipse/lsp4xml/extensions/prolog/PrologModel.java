/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.extensions.prolog;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Charsets;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.parser.ScannerState;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.services.AttributeCompletionItem;
import org.eclipse.lsp4xml.services.XMLCompletions;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.settings.SharedSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * This class holds values that represent the XSI xsd. Can be seen at
 * https://www.w3.org/2001/XMLSchema-instance
 */
public class PrologModel {

	private static final Logger LOGGER = Logger.getLogger(PrologModel.class.getName());

	public static final String VERSION_NAME = "version";
	public static final String ENCODING_NAME = "encoding";
	public static final String STANDALONE_NAME = "standalone";

	public static final String VERSION_1 = "1.0";
	public static final String VERSION_1_1 = "1.1";

	public static final String UTF_8 = Charsets.UTF_8.toString();
	public static final String ISO_8859_1 = Charsets.ISO_8859_1.toString();
	public static final String WINDOWS_1251 = "Windows-1251";
	public static final String WINDOWS_1252 = "Windows-1252";
	public static final String SHIFT_JIS = "Shift JIS";
	public static final String GB2312 = "GB2312";
	public static final String EUC_KR = "EUC-KR";

	public static final String YES = "yes";
	public static final String NO = "no";
	
	
	// Don't change order of list items
	public static final List<String> VERSION_VALUES =  Arrays.asList(VERSION_1, VERSION_1_1);
	public static final List<String> ENCODING_VALUES =  Arrays.asList(UTF_8, ISO_8859_1, WINDOWS_1251, WINDOWS_1252, SHIFT_JIS, GB2312, EUC_KR);
	public static final List<String> STANDALONE_VALUES = Arrays.asList(YES, NO);
	
	public static void computePrologCompletionResponses(int tokenEndOffset, String tag, ICompletionRequest request,
	ICompletionResponse response, boolean inPIState, SharedSettings settings) {
		DOMDocument document = request.getXMLDocument();
		CompletionItem item = new CompletionItem();
		
		item.setLabel("<?xml ... ?>");
		item.setKind(CompletionItemKind.Property);
		item.setFilterText("xml version=\"1.0\" encoding=\"UTF-8\"?>");
		boolean isSnippetsSupported = request.getCompletionSettings().isCompletionSnippetsSupported();
		InsertTextFormat insertText = isSnippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText;
		item.setInsertTextFormat(insertText);
		int closingBracketOffset;
		if (inPIState) {
			closingBracketOffset = XMLCompletions.getOffsetFollowedBy(document.getText(), tokenEndOffset, ScannerState.WithinPI,
					TokenType.PIEnd);
		} else {// prolog state
			closingBracketOffset = XMLCompletions.getOffsetFollowedBy(document.getText(), tokenEndOffset, ScannerState.WithinTag,
					TokenType.PrologEnd);
		}

		if (closingBracketOffset != -1) {
			// Include '?>'
			closingBracketOffset += 2;
		} else {
			closingBracketOffset = XMLCompletions.getOffsetFollowedBy(document.getText(), tokenEndOffset, ScannerState.WithinTag,
					TokenType.StartTagClose);
			if (closingBracketOffset == -1) {
				closingBracketOffset = tokenEndOffset;
			} else {
				closingBracketOffset++;
			}
		}
		int startOffset = tokenEndOffset - tag.length();
		try {
			Range editRange = XMLCompletions.getReplaceRange(startOffset, closingBracketOffset, request);
			String q = settings.formattingSettings.getQuotationAsString();
			String cursor = isSnippetsSupported ? "$0" : "";
			String text = MessageFormat.format("xml version={0}{1}{0} encoding={0}{2}{0}?>" + cursor , q, VERSION_1, UTF_8);
			item.setTextEdit(new TextEdit(editRange, text));
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing getReplaceRange for prolog completion.", e);
		}
		response.addCompletionItem(item);
	}
	

	private static void createCompletionItem(String attrName, boolean canSupportSnippet, boolean generateValue,
			Range editRange, String defaultValue, Collection<String> enumerationValues, String documentation,
			ICompletionResponse response, XMLFormattingOptions formattingsSettings){
		CompletionItem item = new AttributeCompletionItem(attrName, canSupportSnippet, editRange, generateValue,
				defaultValue, enumerationValues, formattingsSettings);
		MarkupContent markup = new MarkupContent();
		markup.setKind(MarkupKind.MARKDOWN);
		
		markup.setValue(StringUtils.getDefaultString(documentation));
		item.setDocumentation(markup);
		response.addCompletionItem(item);
	}

	public static void computeAttributeNameCompletionResponses(ICompletionRequest request, 
	ICompletionResponse response, Range editRange, DOMDocument document, XMLFormattingOptions formattingsSettings)
			throws BadLocationException {

		if (document.hasProlog() == false) {
			return;
		}
		int offset = document.offsetAt(editRange.getStart());
		DOMNode prolog = document.findNodeAt(offset);
		if(!prolog.isProlog()) {
			return;
		}
		boolean isSnippetsSupported = request.getCompletionSettings().isCompletionSnippetsSupported();
		
		if(!prolog.hasAttribute(VERSION_NAME)) {
			createCompletionItem(VERSION_NAME, isSnippetsSupported, true, editRange, VERSION_1, VERSION_VALUES, null, response, formattingsSettings);
		}

		if(!prolog.hasAttribute(ENCODING_NAME)) {
			createCompletionItem(ENCODING_NAME, isSnippetsSupported, true, editRange, UTF_8, ENCODING_VALUES, null, response, formattingsSettings);
		}

		if(!prolog.hasAttribute(STANDALONE_NAME)) {
			createCompletionItem(STANDALONE_NAME, isSnippetsSupported, true, editRange, YES, STANDALONE_VALUES, null, response, formattingsSettings);
		}

	}

	public static void computeValueCompletionResponses(ICompletionRequest request, 
			ICompletionResponse response, Range editRange, DOMDocument document, XMLFormattingOptions formattingSettings) throws BadLocationException {
		
		if (document.hasProlog() == false) {
			return;
		}
		int offset = document.offsetAt(editRange.getStart());
		DOMNode prolog = document.findNodeAt(offset);
		if(!prolog.isProlog()) {
			return;
		}

		DOMAttr attr = prolog.findAttrAt(offset);
		if(VERSION_NAME.equals(attr.getName())) { // version
			createCompletionItemsForValues(VERSION_VALUES, editRange, document, request, response);
		}

		else if(ENCODING_NAME.equals(attr.getName())) { // encoding
			createCompletionItemsForValues(ENCODING_VALUES, editRange, document, request, response);
		}

		else if(STANDALONE_NAME.equals(attr.getName())) {
			createCompletionItemsForValues(STANDALONE_VALUES, editRange, document, request, response);
		}
	}

	private static void createCompletionItemsForValues(Collection<String> enumerationValues, Range editRange, DOMDocument document, ICompletionRequest request, ICompletionResponse response) {
		int sortText = 1;
		CompletionItem item;
		for (String option : enumerationValues) {
			String insertText = request.getInsertAttrValue(option);
			item = new CompletionItem();
			item.setLabel(option);
			item.setFilterText(insertText);
			item.setKind(CompletionItemKind.Enum);
			item.setTextEdit(new TextEdit(editRange, insertText));
			item.setSortText(Integer.toString(sortText));
			sortText++;
			response.addCompletionItem(item);
		}
	}

}