/**
 *  Copyright (c) 2019 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */

package org.eclipse.lsp4xml.extensions.prolog;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.settings.SharedSettings;

/**
 * PrologCompletionParticipant
 */
public class PrologCompletionParticipant extends CompletionParticipantAdapter {
	
	@Override
	public void onAttributeName(boolean generateValue, Range fullRange, ICompletionRequest request,
			ICompletionResponse response, SharedSettings settings) throws Exception {
			PrologModel.computeAttributeNameCompletionResponses(request, response, fullRange, request.getXMLDocument(),
					settings);
	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, boolean addQuotes, ICompletionRequest request,
			ICompletionResponse response, SharedSettings settings) throws Exception {
		PrologModel.computeValueCompletionResponses(request, response, fullRange, request.getXMLDocument(), settings);
	}
	
}