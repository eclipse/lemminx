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
package org.eclipse.lsp4xml.contentmodel.participants;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4xml.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.services.extensions.HoverParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;

/**
 * Extension to support XML hover based on content model (XML Schema completion,
 * etc)
 */
public class ContentModelHoverParticipant extends HoverParticipantAdapter {

	@Override
	public Hover onTag(IHoverRequest completionRequest) throws Exception {
		Element node = (Element) completionRequest.getNode();
		CMElementDeclaration cmElement = ContentModelManager.getInstance().findCMElement(node);
		if (cmElement != null) {
			String doc = cmElement.getDocumentation();
			if (doc != null && doc.length() > 0) {
				MarkupContent content = new MarkupContent();
				content.setValue(doc);
				return new Hover(content);
			}
		}
		return null;
	}

	@Override
	public Hover onAttributeName(IHoverRequest completionRequest) throws Exception {
		Element element = (Element) completionRequest.getNode();
		CMElementDeclaration cmElement = ContentModelManager.getInstance().findCMElement(element);
		if (cmElement != null) {
			String attributeName = completionRequest.getCurrentAttributeName();
			CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
			if (cmAttribute != null) {
				String doc = cmAttribute.getDocumentation();
				if (doc != null && doc.length() > 0) {
					MarkupContent content = new MarkupContent();
					content.setValue(doc);
					return new Hover(content);
				}
			}
		}
		return null;
	}

}
