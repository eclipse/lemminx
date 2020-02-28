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

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.settings.XMLCompletionSettings;
import org.eclipse.lemminx.settings.XMLFormattingOptions;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * Completion request implementation.
 *
 */
class CompletionRequest extends AbstractPositionRequest implements ICompletionRequest {

	private final XMLCompletionSettings completionSettings;

	private final XMLFormattingOptions formattingSettings;

	private Range replaceRange;

	private XMLGenerator generator;

	private boolean hasOpenBracket;

	private boolean addQuotes;

	public CompletionRequest(DOMDocument xmlDocument, Position position, SharedSettings settings,
			XMLExtensionsRegistry extensionsRegistry) throws BadLocationException {
		super(xmlDocument, position, extensionsRegistry);
		this.formattingSettings = settings.getFormattingSettings();
		this.completionSettings = settings.getCompletionSettings();
	}
	
	@Override
	protected DOMNode findNodeAt(DOMDocument xmlDocument, int offset) {
		return xmlDocument.findNodeBefore(offset);
	}

	@Override
	public XMLFormattingOptions getFormattingSettings() {
		return formattingSettings;
	}

	@Override
	public XMLCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	public void setReplaceRange(Range replaceRange) {
		this.replaceRange = replaceRange;
	}

	@Override
	public Range getReplaceRange() {
		return replaceRange;
	}

	public XMLGenerator getXMLGenerator() throws BadLocationException {
		if (generator == null) {
			generator = new XMLGenerator(getFormattingSettings(), isAutoCloseTags(),
					getLineIndentInfo().getWhitespacesIndent(), getLineIndentInfo().getLineDelimiter(),
					isCompletionSnippetsSupported(), 0);
		}
		return generator;
	}

	@Override
	public String getFilterForStartTagName(String tagName) {
		if (hasOpenBracket) {
			return "<" + tagName;
		}
		return tagName;
	}

	public void setHasOpenBracket(boolean hasOpenBracket) {
		this.hasOpenBracket = hasOpenBracket;
	}

	public void setAddQuotes(boolean addQuotes) {
		this.addQuotes = addQuotes;
	}

	public boolean isAddQuotes() {
		return addQuotes;
	}

	@Override
	public String getInsertAttrValue(String value) {
		if (!addQuotes) {
			return value;
		}
		String quotation = getQuotation();
		return quotation + value + quotation;
	}

	private String getQuotation() {
		String quotation = formattingSettings != null ? formattingSettings.getQuotationAsString() : null;
		return StringUtils.isEmpty(quotation) ? XMLFormattingOptions.DEFAULT_QUOTATION : quotation;
	}

	@Override
	public boolean canSupportMarkupKind(String kind) {
		return completionSettings != null && completionSettings.getCompletionCapabilities() != null
				&& completionSettings.getCompletionCapabilities().getCompletionItem() != null
				&& completionSettings.getCompletionCapabilities().getCompletionItem().getDocumentationFormat() != null
				&& completionSettings.getCompletionCapabilities().getCompletionItem().getDocumentationFormat()
						.contains(kind);
	}

	@Override
	public boolean isCompletionSnippetsSupported() {
		return getCompletionSettings().isCompletionSnippetsSupported();
	}

	@Override
	public boolean isAutoCloseTags() {
		return getCompletionSettings().isAutoCloseTags();
	}

	@Override
	public InsertTextFormat getInsertTextFormat() {
		return isCompletionSnippetsSupported() ? InsertTextFormat.Snippet : InsertTextFormat.PlainText;
	}

}