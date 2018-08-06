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
package org.eclipse.lsp4xml.extensions;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Range;

/**
 * Completion request API.
 *
 */
public interface ICompletionRequest extends IPositionRequest {

	Range getReplaceRange();

	FormattingOptions getFormattingSettings();

	CompletionSettings getCompletionSettings();
}
