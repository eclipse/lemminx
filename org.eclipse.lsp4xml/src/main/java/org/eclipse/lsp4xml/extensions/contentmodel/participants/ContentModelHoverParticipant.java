/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.services.XSISchemaModel;
import org.eclipse.lsp4xml.services.extensions.HoverParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;

/**
 * Extension to support XML hover based on content model (XML Schema completion,
 * etc)
 */
public class ContentModelHoverParticipant extends HoverParticipantAdapter {

	@Override
	public Hover onTag(IHoverRequest hoverRequest) throws Exception {
		try {
			ContentModelManager contentModelManager = hoverRequest.getComponent(ContentModelManager.class);
			DOMElement node = (DOMElement) hoverRequest.getNode();
			CMElementDeclaration cmElement = contentModelManager.findCMElement(node);
			if (cmElement != null) {
				String doc = cmElement.getDocumentation();
				if (doc != null && doc.length() > 0) {
					MarkupContent content = new MarkupContent();
					content.setKind(MarkupKind.PLAINTEXT);
					content.setValue(doc);
					return new Hover(content, hoverRequest.getTagRange());
				}
			}
		} catch (CacheResourceDownloadingException e) {
			return getCacheWarningHover(e);
		}
		return null;
	}

	@Override
	public Hover onAttributeName(IHoverRequest hoverRequest) throws Exception {
		
		DOMAttr attribute = (DOMAttr) hoverRequest.getNode();

		//Attempts to compute specifically for XSI related attributes since
		//the XSD itself does not have enough information. Should create a mock XSD eventually.
		Hover temp = XSISchemaModel.computeHoverResponse(attribute, hoverRequest);
		if(temp != null) {
			return temp;
		}
		
		try {
			ContentModelManager contentModelManager = hoverRequest.getComponent(ContentModelManager.class);
			
			CMElementDeclaration cmElement = contentModelManager.findCMElement(attribute.getOwnerElement());
			if (cmElement != null) {
				String attributeName = attribute.getName();
				CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
				if (cmAttribute != null) {
					String doc = cmAttribute.getDocumentation();
					if (doc != null && doc.length() > 0) {
						MarkupContent content = new MarkupContent();
						content.setKind(MarkupKind.PLAINTEXT);
						content.setValue(doc);
						return new Hover(content);
					}
				}
			}
		} catch (CacheResourceDownloadingException e) {
			return getCacheWarningHover(e);
		}
		return null;
	}

	private Hover getCacheWarningHover(CacheResourceDownloadingException e) {
		// Here cache is enabled and some XML Schema, DTD, etc are loading
		MarkupContent content = new MarkupContent();
		content.setKind(MarkupKind.PLAINTEXT);
		content.setValue("Cannot process " + (e.isDTD() ? "DTD" : "XML Schema") + " hover: " + e.getMessage());
		return new Hover(content);
	}
}
