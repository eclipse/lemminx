/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.extensions.CompletionSettings;
import org.eclipse.lsp4xml.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * Completion request implementation.
 *
 */
class CompletionRequest extends AbstractPositionRequest implements ICompletionRequest {

	private final CompletionSettings completionSettings;
	
	private final FormattingOptions formattingSettings;

	public CompletionRequest(XMLDocument xmlDocument, Position position, CompletionSettings completionSettings, FormattingOptions formattingSettings)
			throws BadLocationException {
		super(xmlDocument, position);
		this.formattingSettings = formattingSettings;
		this.completionSettings = completionSettings;
	}

	@Override
	public FormattingOptions getFormattingSettings() {
		return formattingSettings;
	}
	
	@Override
	public CompletionSettings getCompletionSettings() {
		return completionSettings;
	}
}
