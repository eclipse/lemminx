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
package org.eclipse.lsp4xml.emmet.participants;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.emmet.utils.EmmetHelper;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;

/**
 * Extension to support XML completion with Emmet.
 */
public class EmmetCompletionParticipant extends CompletionParticipantAdapter {
	
	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) throws Exception {
		Position position = request.getPosition();
		TextDocument document = request.getXMLDocument().getTextDocument();
		String syntax = document.getUri().endsWith(".xsl") ? "xsl" : "xml";
		CompletionList list = EmmetHelper.doComplete(document, position, syntax, null);
		if (list != null) {
			for (CompletionItem item : list.getItems()) {
				response.addCompletionItem(item);
			}
		}
	}

}
