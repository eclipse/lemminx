/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.lsp4j.TextDocumentItem;

/**
 * Language model cache.
 * 
 * @see https://github.com/Microsoft/vscode/blob/master/extensions/json-language-features/server/src/languageModelCache.ts
 *
 * @param <T>
 */
public class LanguageModelCache<T> {

	private final Map<String, LanguageModeInfo> languageModels;
	private final Function<TextDocument, T> parse;

	class LanguageModeInfo {

		public final int version;
		public final String languageId;
		public long cTime;
		public T languageModel;

		public LanguageModeInfo(T languageModel, int version, String languageId, long cTime) {
			this.languageModel = languageModel;
			this.version = version;
			this.languageId = languageId;
			this.cTime = cTime;
		}
	}

	public LanguageModelCache(int maxEntries, int cleanupIntervalTimeInSec, Function<TextDocument, T> parse) {
		this.languageModels = new HashMap<>();
		this.parse = parse;
	}

	public T get(TextDocumentItem document) {
		int version = document.getVersion();
		String languageId = document.getLanguageId();
		String uri = document.getUri();
		LanguageModeInfo languageModelInfo = languageModels.get(uri);
		if (languageModelInfo != null && languageModelInfo.version == version
				&& Objects.equals(languageId, languageModelInfo.languageId)) {
			languageModelInfo.cTime = System.currentTimeMillis();
			return languageModelInfo.languageModel;
		}
		TextDocument textDocument = document instanceof TextDocument ? (TextDocument) document
				: new TextDocument(document);
		T languageModel = parse.apply(textDocument);
		languageModels.put(uri, new LanguageModeInfo(languageModel, version, languageId, System.currentTimeMillis()));
		return languageModel;
	}

	public void onDocumentRemoved(String uri) {
		languageModels.remove(uri);
	}

}
