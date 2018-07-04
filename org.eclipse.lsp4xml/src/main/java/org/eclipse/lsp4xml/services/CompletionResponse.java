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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4xml.extensions.ICompletionResponse;

/**
 * Completion response implementation.
 *
 */
class CompletionResponse extends CompletionList implements ICompletionResponse {

	public CompletionResponse() {
		super.setIsIncomplete(false);
	}

	@Override
	public void addCompletionItem(CompletionItem completionItem) {
		super.getItems().add(completionItem);
	}
}
