/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.extensions.xsi.XSISchemaModel;
import org.eclipse.lsp4xml.services.extensions.HoverParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;
import org.eclipse.lsp4xml.utils.MarkupContentFactory;
import org.eclipse.lsp4xml.utils.MarkupContentFactory.IMarkupKindSupport;

/**
 * Extension to support XML hover based on content model (XML Schema completion,
 * etc)
 */
public class ContentModelHoverParticipant extends HoverParticipantAdapter {

	@Override
	public String onTag(IHoverRequest hoverRequest) throws Exception {
		try {
			ContentModelManager contentModelManager = hoverRequest.getComponent(ContentModelManager.class);
			DOMElement node = (DOMElement) hoverRequest.getNode();
			CMElementDeclaration cmElement = contentModelManager.findCMElement(node);
			if (cmElement != null) {
				MarkupContent content = XMLGenerator.createMarkupContent(cmElement, hoverRequest);
				if (content != null) {
					return content.getValue();
				}
			}
		} catch (CacheResourceDownloadingException e) {
			return getCacheWarningHover(e, hoverRequest);
		}
		return null;
	}

	@Override
	public String onAttributeName(IHoverRequest hoverRequest) throws Exception {
		DOMAttr attribute = (DOMAttr) hoverRequest.getNode();
		try {
			ContentModelManager contentModelManager = hoverRequest.getComponent(ContentModelManager.class);
			CMElementDeclaration cmElement = contentModelManager.findCMElement(attribute.getOwnerElement());
			if (cmElement != null) {
				String attributeName = attribute.getName();
				CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
				if (cmAttribute != null) {
					MarkupContent content = XMLGenerator.createMarkupContent(cmAttribute, cmElement, hoverRequest);
					if (content != null) {
						return content.getValue();
					}
				}
			}
		} catch (CacheResourceDownloadingException e) {
			return getCacheWarningHover(e, hoverRequest);
		}
		return null;
	}

	@Override
	public String onAttributeValue(IHoverRequest hoverRequest) throws Exception {
		DOMAttr attribute = (DOMAttr) hoverRequest.getNode();

		// Attempts to compute specifically for XSI related attributes since
		// the XSD itself does not have enough information. Should create a mock XSD
		// eventually.
		String temp = XSISchemaModel.computeHoverResponse(attribute, hoverRequest);
		if (temp != null) {
			return temp;
		}

		try {
			ContentModelManager contentModelManager = hoverRequest.getComponent(ContentModelManager.class);
			CMElementDeclaration cmElement = contentModelManager.findCMElement(attribute.getOwnerElement());
			if (cmElement != null) {
				String attributeName = attribute.getName();
				CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);

				String attributeValue = attribute.getValue();
				if (cmAttribute != null) {
					MarkupContent content = XMLGenerator.createMarkupContent(cmAttribute, attributeValue, cmElement,
							hoverRequest);
					if (content != null) {
						return content.getValue();
					}
				}
			}
		} catch (CacheResourceDownloadingException e) {
			return getCacheWarningHover(e, hoverRequest);
		}
		return null;
	}

	private static String getCacheWarningHover(CacheResourceDownloadingException e, IMarkupKindSupport support) {
		// Here cache is enabled and some XML Schema, DTD, etc are loading
		MarkupContent content = MarkupContentFactory.createMarkupContent(
				"Cannot process " + (e.isDTD() ? "DTD" : "XML Schema") + " hover: " + e.getMessage(),
				MarkupKind.MARKDOWN, support);
		return content.getValue();
	}
}
