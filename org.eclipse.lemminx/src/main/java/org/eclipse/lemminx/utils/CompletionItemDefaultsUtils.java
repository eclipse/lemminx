/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.lemminx.services.CompletionResponse;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemDefaults;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * CompletionItemDefaultsUtils for implementing itemDefaults in completion list
 */
public class CompletionItemDefaultsUtils {

	private static final String ITEM_DEFAULTS_EDIT_RANGE = "editRange";

	private static final String ITEM_DEFAULTS_INSERT_TEXT_FORMAT = "insertTextFormat";

	/**
	 * Processes the completion list and manages item defaults.
	 *
	 * @param completionResponse the completion response
	 * @param sharedSettings     the shared settings
	 */
	public static void process(CompletionResponse completionResponse, SharedSettings sharedSettings) {
		CompletionItemDefaults itemDefaults = new CompletionItemDefaults();
		List<CompletionItem> completionList = completionResponse.getItems();
		if (sharedSettings.getCompletionSettings().isCompletionListItemDefaultsSupport(ITEM_DEFAULTS_EDIT_RANGE)) {
			setToMostCommonEditRange(completionList, itemDefaults);
			completionResponse.setItemDefaults(itemDefaults);
		}
		if (sharedSettings.getCompletionSettings()
				.isCompletionListItemDefaultsSupport(ITEM_DEFAULTS_INSERT_TEXT_FORMAT)) {
			setToMostCommonInsertTextFormat(completionList, itemDefaults);
			completionResponse.setItemDefaults(itemDefaults);
		}
	}

	/**
	 * Sets the most common editRange in the completion list in item
	 * defaults and processes the completion list.
	 *
	 * @param completionList the completion list
	 * @param itemDefaults   the item defaults
	 */
	private static void setToMostCommonEditRange(List<CompletionItem> completionList,
			CompletionItemDefaults itemDefaults) {
		Map<Range, List<CompletionItem>> itemsByRange = completionList.stream()
				.collect(Collectors.groupingBy(item -> item.getTextEdit().getLeft().getRange()));
		int maxCount = 0;
		Range mostCommonRange = null;
		for (Map.Entry<Range, List<CompletionItem>> entry : itemsByRange.entrySet()) {
			int currentSize = entry.getValue().size();
			if (currentSize > maxCount) {
				maxCount = currentSize;
				mostCommonRange = entry.getKey();
			}
		}
		itemsByRange.get(mostCommonRange).forEach(item -> {
			item.setTextEditText(item.getTextEdit().getLeft().getNewText());
			item.setTextEdit(null);
		});
		itemDefaults.setEditRange(Either.forLeft(mostCommonRange));
		completionList = itemsByRange.values().stream().flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Sets the most common InsertTextFormat in the completion list in item
	 * defaults and processes the completion list.
	 *
	 * @param completionList the completion list
	 * @param itemDefaults   the item defaults
	 */
	private static void setToMostCommonInsertTextFormat(List<CompletionItem> completionList,
			CompletionItemDefaults itemDefaults) {
		Map<InsertTextFormat, List<CompletionItem>> itemsByInsertTextFormat = completionList.stream()
				.filter(item -> item.getInsertTextFormat() != null)
				.collect(Collectors.groupingBy(item -> item.getInsertTextFormat()));
		int maxCount = 0;
		InsertTextFormat mostCommonInsertTextFormat = null;
		for (Map.Entry<InsertTextFormat, List<CompletionItem>> entry : itemsByInsertTextFormat.entrySet()) {
			int currentSize = entry.getValue().size();
			if (currentSize > maxCount) {
				maxCount = currentSize;
				mostCommonInsertTextFormat = entry.getKey();
			}
		}
		if (mostCommonInsertTextFormat == null) {
			return;
		}
		itemsByInsertTextFormat.get(mostCommonInsertTextFormat).forEach(item -> {
			item.setInsertTextFormat(null);
		});
		itemDefaults.setInsertTextFormat(mostCommonInsertTextFormat);
		completionList = itemsByInsertTextFormat.values().stream().flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
}
