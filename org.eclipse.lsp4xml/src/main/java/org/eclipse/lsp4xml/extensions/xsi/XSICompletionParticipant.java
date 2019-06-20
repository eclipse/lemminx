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
package org.eclipse.lsp4xml.extensions.xsi;

import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;

/**
 * XSICompletionParticipant
 */
public class XSICompletionParticipant extends CompletionParticipantAdapter {

	@Override
	public void onAttributeName(boolean generateValue, ICompletionRequest request, ICompletionResponse response)
			throws Exception {
		XSISchemaModel.computeCompletionResponses(request, response, request.getXMLDocument(), generateValue,
				request.getFormattingSettings());
	}

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response)
			throws Exception {
		XSISchemaModel.computeValueCompletionResponses(request, response, request.getXMLDocument());
	}

}