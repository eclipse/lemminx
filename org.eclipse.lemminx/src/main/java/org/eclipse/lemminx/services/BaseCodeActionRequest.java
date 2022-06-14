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
import org.eclipse.lemminx.services.extensions.codeaction.IBaseCodeActionRequest;
import org.eclipse.lemminx.settings.SharedSettings;

/**
 * Base implementation for textDocument/codeAction and codeAction/request.
 * 
 * @author Angelo ZERR
 *
 */
class BaseCodeActionRequest implements IBaseCodeActionRequest {

	private final DOMDocument document;

	private final IComponentProvider componentProvider;

	private final SharedSettings sharedSettings;

	public BaseCodeActionRequest(DOMDocument document, IComponentProvider componentProvider,
			SharedSettings sharedSettings) {
		this.document = document;
		this.componentProvider = componentProvider;
		this.sharedSettings = sharedSettings;
	}

	@Override
	public <T> T getComponent(Class clazz) {
		return componentProvider.getComponent(clazz);
	}

	@Override
	public DOMDocument getDocument() {
		return document;
	}

	@Override
	public SharedSettings getSharedSettings() {
		return sharedSettings;
	}

}
