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
package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.CompletionCapabilities;

/**
 * A wrapper around LSP {@link CompletionCapabilities}.
 *
 */
public class CompletionSettings {

	private CompletionCapabilities completionCapabilities;

	private boolean autoCloseTags;

	public CompletionSettings(boolean autoCloseTags) {
		this.autoCloseTags = autoCloseTags;
	}

	public CompletionSettings() {
		this(true);
	}

	public void setCapabilities(CompletionCapabilities completionCapabilities) {
		this.completionCapabilities = completionCapabilities;
	}

	public CompletionCapabilities getCompletionCapabilities() {
		return completionCapabilities;
	}

	/**
	 * Tag should be autoclosed with an end tag.
	 * @param autoCloseTags
	 */
	public void setAutoCloseTags(boolean autoCloseTags) {
		this.autoCloseTags = autoCloseTags;
	}

	/**
	 * If tag should be autoclosed with an end tag.
	 * @return
	 */
	public boolean isAutoCloseTags() {
		return autoCloseTags;
	}

	/**
	 * Returns <code>true</code> if the client support snippet and
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the client support snippet and
	 *         <code>false</code> otherwise.
	 */
	public boolean isCompletionSnippetsSupported() {
		return completionCapabilities != null && completionCapabilities.getCompletionItem() != null
				&& completionCapabilities.getCompletionItem().getSnippetSupport() != null
				&& completionCapabilities.getCompletionItem().getSnippetSupport();
	}
}
