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
import org.eclipse.lsp4xml.services.extensions.ITypeDefinitionRequest;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

/**
 * Type definition request implementation.
 *
 */
class TypeDefinitionRequest extends AbstractPositionRequest implements ITypeDefinitionRequest {

	public TypeDefinitionRequest(DOMDocument xmlDocument, Position position, XMLExtensionsRegistry extensionsRegistry)
			throws BadLocationException {
		super(xmlDocument, position, extensionsRegistry);
	}

}
