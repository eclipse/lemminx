/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.extensions.general;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.extensions.general.completion.FilePathCompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * FilePathPlugin
 */
public class FilePathPlugin implements IXMLExtension {

	private final FilePathCompletionParticipant completionParticipant;

	public FilePathPlugin() {
		completionParticipant = new FilePathCompletionParticipant();
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		registry.registerCompletionParticipant(completionParticipant);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		registry.unregisterCompletionParticipant(completionParticipant);
	}

	@Override
	public void doSave(ISaveContext context) {

	}

	
}