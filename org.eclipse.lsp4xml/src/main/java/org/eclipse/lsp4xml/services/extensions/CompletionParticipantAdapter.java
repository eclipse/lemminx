/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.services.extensions;

import org.eclipse.lsp4j.Range;

/**
 * Completion participant adapter.
 *
 */
public class CompletionParticipantAdapter implements ICompletionParticipant {

	@Override
	public void onTagOpen(ICompletionRequest completionRequest, ICompletionResponse completionResponse)
			throws Exception {
		// Do nothing
	}

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) throws Exception {
		// Do nothing
	}

	@Override
	public void onAttributeName(boolean generateValue, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) throws Exception {
		// Do nothing
	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, boolean addQuotes, ICompletionRequest request,
			ICompletionResponse response) throws Exception {
		// Do nothing
	}

}
