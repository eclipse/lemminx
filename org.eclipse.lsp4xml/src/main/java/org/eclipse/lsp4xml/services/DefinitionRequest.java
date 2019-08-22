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

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * Definition request implementation.
 *
 */
class DefinitionRequest extends AbstractPositionRequest implements IDefinitionRequest {

	public DefinitionRequest(DOMDocument xmlDocument, Position position, XMLExtensionsRegistry extensionsRegistry)
			throws BadLocationException {
		super(xmlDocument, position, extensionsRegistry);
	}

}
