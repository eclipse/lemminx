/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.XMLModel;
import org.eclipse.lemminx.extensions.contentmodel.model.CMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelProvider;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.StringUtils;

public class CMRelaxNGContentModelProvider implements ContentModelProvider {

	// TODO: should this be different for RelaxNG?
	private static final String XML_MODEL_BINDING_KIND = "xml-model";

	@Override
	public boolean adaptFor(DOMDocument document, boolean internal) {
		if (internal) {
			return false;
		}
		return document.getXMLModels().stream().anyMatch(xmlModel -> DOMUtils.isRelaxNG(xmlModel.getHref()));
	}

	@Override
	public boolean adaptFor(String uri) {
		return false;
	}

	@Override
	public Collection<Identifier> getIdentifiers(DOMDocument xmlDocument, String namespaceURI) {
		List<XMLModel> xmlModels = xmlDocument.getXMLModels();
		if (xmlModels.isEmpty()) {
			return Collections.emptyList();
		}
		Collection<Identifier> identifiers = new ArrayList<>();
		for (XMLModel xmlModel : xmlModels) {
			String href = xmlModel.getHref();
			if (!StringUtils.isEmpty(href) && DOMUtils.isRelaxNG(href)) {
				identifiers.add(new Identifier(null, href, xmlModel.getHrefNode(), XML_MODEL_BINDING_KIND));
			}
		}
		return identifiers;
	}

	@Override
	public CMDocument createCMDocument(String key, boolean resolveExternalEntities) {
		try {
			return new CMRelaxNGDocument(key);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public CMDocument createInternalCMDocument(DOMDocument xmlDocument, boolean resolveExternalEntities) {
		// Not relevant for RelaxNG schemas, only works for .dtd
		return null;
	}

}
