/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.xmlmodel.contentmodel;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lemminx.utils.StringUtils;

/**
 * 
 * Content model provider to manage href of xml-model processing instruction.
 *
 */
public class CMXMLModelContentModelProvider implements ContentModelProvider {

	private final ContentModelManager modelManager;

	public CMXMLModelContentModelProvider(ContentModelManager modelManager) {
		this.modelManager = modelManager;
	}

	@Override
	public boolean adaptFor(DOMDocument document, boolean internal) {
		if (internal) {
			return false;
		}
		return document.hasXMLModel();
	}

	@Override
	public boolean adaptFor(String uri) {
		return false;
	}

	@Override
	public Collection<String> getSystemIds(DOMDocument xmlDocument, String namespaceURI) {
		return xmlDocument.getXMLModels().stream().map(node -> node.getHref())
				.filter(href -> !StringUtils.isEmpty(href)).collect(Collectors.toList());
	}

	@Override
	public CMDocument createCMDocument(String uri) {
		ContentModelProvider modelProvider = modelManager.getModelProviderByURI(uri);
		return modelProvider != null ? modelProvider.createCMDocument(uri) : null;
	}

	@Override
	public CMDocument createInternalCMDocument(DOMDocument xmlDocument) {
		return null;
	}

}
