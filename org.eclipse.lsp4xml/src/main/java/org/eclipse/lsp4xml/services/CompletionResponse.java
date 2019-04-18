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
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;

/**
 * Completion response implementation.
 *
 */
class CompletionResponse extends CompletionList implements ICompletionResponse {

	private transient List<String> seenAttributes;
	private transient boolean hasSomeItemFromGrammar;

	public CompletionResponse() {
		super.setIsIncomplete(false);
	}

	public void addCompletionItem(CompletionItem completionItem, boolean fromGrammar) {
		if (fromGrammar) {
			hasSomeItemFromGrammar = true;

		}
		if(completionItem == null) {
			return;
		}
		addCompletionItem(completionItem);
	}

	/**
	 * Indicates to not create anymore completion items.
	 */
	public void doNotCreateAnymoreItems() {
		this.hasSomeItemFromGrammar = true;
	}

	@Override
	public void addCompletionItem(CompletionItem completionItem) {
		super.getItems().add(completionItem);
	}

	@Override
	public boolean hasSomeItemFromGrammar() {
		return hasSomeItemFromGrammar;
	}

	@Override
	public boolean hasSeen(String label) {
		/*
		 * if (node != null && node.hasAttribute(attribute)) { return true; }
		 */
		return seenAttributes != null ? seenAttributes.contains(label) : false;
	}

	@Override
	public void addCompletionItemAsSeen(CompletionItem completionItem) {
		hasSomeItemFromGrammar = true;
		if (seenAttributes == null) {
			seenAttributes = new ArrayList<>();
		}
		// TODO: Add quotations to the completion item.
		seenAttributes.add(completionItem.getLabel());
		addCompletionItem(completionItem);
	}

}
