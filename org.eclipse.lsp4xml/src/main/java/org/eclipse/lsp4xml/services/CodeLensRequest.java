/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.services;

import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeLensRequest;
import org.eclipse.lsp4xml.settings.XMLCodeLensSettings;

/**
 * CodeLens request
 * 
 * @author Angelo ZERR
 *
 */
class CodeLensRequest implements ICodeLensRequest {

	private final DOMDocument document;

	private final XMLCodeLensSettings settings;

	public CodeLensRequest(DOMDocument document, XMLCodeLensSettings settings) {
		this.document = document;
		this.settings = settings;
	}

	@Override
	public DOMDocument getDocument() {
		return document;
	}

	@Override
	public boolean isSupportedByClient(String kind) {
		return settings.isSupportedByClient(kind);
	}

}
