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

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.settings.XMLCompletionSettings;
import org.eclipse.lsp4xml.settings.SharedSettings;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * Completion request implementation.
 *
 */
class CompletionRequest extends AbstractPositionRequest implements ICompletionRequest {

	private final XMLCompletionSettings completionSettings;

	private final XMLFormattingOptions formattingSettings;

	private final XMLExtensionsRegistry extensionsRegistry;

	private Range replaceRange;

	private XMLGenerator generator;

	private boolean hasOpenBracket;

	private boolean addQuotes;

	public CompletionRequest(DOMDocument xmlDocument, Position position, SharedSettings settings,
			XMLExtensionsRegistry extensionsRegistry) throws BadLocationException {
		super(xmlDocument, position);
		this.formattingSettings = settings.getFormattingSettings();
		this.completionSettings = settings.getCompletionSettings();
		this.extensionsRegistry = extensionsRegistry;
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
			generator = new XMLGenerator(getFormattingSettings(), getCompletionSettings().isAutoCloseTags(),
					getLineIndentInfo().getWhitespacesIndent(), getLineIndentInfo().getLineDelimiter(),
					getCompletionSettings().isCompletionSnippetsSupported(), 0);
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

	@Override
	public <T> T getComponent(Class clazz) {
		return extensionsRegistry.getComponent(clazz);
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

}