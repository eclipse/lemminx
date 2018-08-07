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
import org.eclipse.lsp4xml.contentmodel.model.CMElement;
import org.eclipse.lsp4xml.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.services.extensions.HoverParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;

/**
 * Extension to support XML hover based on content model (XML Schema completion,
 * etc)
 */
public class ContentModelHoverParticipant extends HoverParticipantAdapter {

	@Override
	public Hover onTag(IHoverRequest request) {
		try {
			Node node = request.getNode();
			CMElement cmlElement = ContentModelManager.getInstance().findCMElement(node);
			if (cmlElement != null) {
				String doc = cmlElement.getDocumentation();
				if (doc != null && doc.length() > 0) {
					MarkupContent content = new MarkupContent();
					content.setValue(doc);
					return new Hover(content);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
