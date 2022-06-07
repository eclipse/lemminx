/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

/**
 * Code action request implementation.
 * 
 * @author Angelo ZERR
 *
 */
public class CodeActionRequest extends BaseCodeActionRequest implements ICodeActionRequest {

	private final Diagnostic diagnostic;

	private final Range range;

	public CodeActionRequest(Diagnostic diagnostic, Range range, DOMDocument document,
			IComponentProvider componentProvider, SharedSettings sharedSettings) {
		super(document, componentProvider, sharedSettings);
		this.diagnostic = diagnostic;
		this.range = range;
	}

	@Override
	public Diagnostic getDiagnostic() {
		return diagnostic;
	}

	@Override
	public Range getRange() {
		return range;
	}

}
