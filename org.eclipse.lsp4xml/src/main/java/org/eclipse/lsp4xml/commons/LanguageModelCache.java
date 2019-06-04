/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Language model cache.
 * 
 * @see https://github.com/Microsoft/vscode/blob/master/extensions/json-language-features/server/src/languageModelCache.ts
 *
 * @param <T> model type (ex : DOMDocument for XML).
 */
public class LanguageModelCache<T> {

	private final Map<String, LanguageModeInfo> languageModels;
	private final BiFunction<TextDocument, CancelChecker, T> parse;
	private final ITextDocumentFactory documentFactory;

	class LanguageModeInfo {

		public int version;
		public String languageId;
		public long cTime;
		public T languageModel;

		public LanguageModeInfo(T languageModel, int version, String languageId, long cTime) {
			this.languageModel = languageModel;
			this.version = version;
			this.languageId = languageId;
			this.cTime = cTime;
		}

		/**
		 * Returns true if the given version and language id checks the version and
		 * language id from the last cached model and false otherwise.
		 * 
		 * @param version    the version to chseck
		 * @param languageId the language id to check
		 * @return true if the given version and language id checks the version and
		 *         language id from the last cached model and false otherwise.
		 */
		public boolean check(int version, String languageId) {
			if (this.version > version) {
				// Stop the process because the given version is more old than the cached model
				// version
				// This case comes from when there are an old request which comes after a new
				// request.
				throw new CancellationException("The version '" + version
						+ "' is older than the cached model version '" + this.version + "'.");
			}
			if (this.version == version && Objects.equals(languageId, this.languageId)) {
				cTime = System.currentTimeMillis();
				return true;
			}
			return false;
		}
	}

	public LanguageModelCache(int maxEntries, int cleanupIntervalTimeInSec, ITextDocumentFactory documentFactory,
			BiFunction<TextDocument, CancelChecker, T> parse) {
		this.languageModels = new HashMap<>();
		this.parse = parse;
		this.documentFactory = documentFactory;
	}

	public T get(TextDocumentItem document) {
		int version = document.getVersion();
		String languageId = document.getLanguageId();
		String uri = document.getUri();
		// get the language model information
		LanguageModeInfo languageModelInfo = getLanguageModelInfo(uri);

		if (languageModelInfo.check(version, languageId)) {
			// The language model checks the current version, returns the cached model (ex:
			// DOMDocument instance)
			return languageModelInfo.languageModel;
		}

		// Parse the model (ex: DOM document) from the given text document.
		synchronized (languageModelInfo) {
			if (languageModelInfo.check(version, languageId)) {
				return languageModelInfo.languageModel;
			}
			TextDocument textDocument = document instanceof TextDocument ? (TextDocument) document
					: documentFactory.createDocument(document);
			languageModelInfo.languageModel = parse.apply(textDocument,
					new TextDocumentVersionChecker(textDocument, version));
			languageModelInfo.version = version;
			languageModelInfo.languageId = languageId;
		}
		return languageModelInfo.languageModel;
	}

	/**
	 * Returns the language model information for the given uri
	 * 
	 * @param uri document uri
	 * @return the language model information for the given uri
	 */
	private LanguageModeInfo getLanguageModelInfo(String uri) {
		LanguageModeInfo languageModelInfo = languageModels.get(uri);
		if (languageModelInfo != null) {
			return languageModelInfo;
		}
		// The information doesn't exists, create it
		synchronized (languageModels) {
			languageModelInfo = languageModels.get(uri);
			if (languageModelInfo != null) {
				return languageModelInfo;
			}
			languageModelInfo = new LanguageModeInfo(null, -1, null, -1);
			languageModels.put(uri, languageModelInfo);
		}
		return languageModelInfo;
	}

	public void onDocumentRemoved(String uri) {
		synchronized (languageModels) {
			languageModels.remove(uri);
		}
	}

}
