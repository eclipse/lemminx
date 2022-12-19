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
package org.eclipse.lemminx.extensions.xinclude;

import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.extensions.IDocumentLinkParticipant;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4j.InitializeParams;

/**
 * XInclude Plugin.
 */
public class XIncludePlugin implements IXMLExtension {

	private ContentModelManager contentModelManager;

	private final IDocumentLinkParticipant documentLinkParticipant;

	public XIncludePlugin() {
		documentLinkParticipant = new XIncludeDocumentLinkParticipant();
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		// xinclude participant
		registry.registerDocumentLinkParticipant(documentLinkParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		// xinclude participant
		registry.unregisterDocumentLinkParticipant(documentLinkParticipant);
	}

	public ContentModelManager getContentModelManager() {
		return contentModelManager;
	}
}
