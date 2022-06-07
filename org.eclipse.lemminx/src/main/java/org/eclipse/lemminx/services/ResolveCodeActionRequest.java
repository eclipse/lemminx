/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.data.DataEntryField;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionResolverRequest;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.CodeAction;

/**
 * Resolve code action request implementation.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolveCodeActionRequest extends BaseCodeActionRequest implements ICodeActionResolverRequest {

	private final CodeAction unresolved;

	private final String participantId;

	public ResolveCodeActionRequest(CodeAction unresolved, DOMDocument document, IComponentProvider componentProvider,
			SharedSettings sharedSettings) {
		super(document, componentProvider, sharedSettings);
		this.unresolved = unresolved;
		this.participantId = DataEntryField.getParticipantId(unresolved.getData());
	}

	@Override
	public CodeAction getUnresolved() {
		return unresolved;
	}

	@Override
	public String getParticipantId() {
		return participantId;
	}

	@Override
	public String getDataProperty(String property) {
		return DataEntryField.getProperty(unresolved.getData(), property);
	}
}
