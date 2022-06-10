/**
 * Copyright (c) 2020 Red Hat Inc. and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel;

import org.eclipse.lemminx.extensions.contentmodel.participants.ContentModelCodeActionParticipant;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

public class ContentModelCodeActionTest {

	private ContentModelCodeActionParticipant cmCodeActionParticipant;

	@Test
	public void codeActionParticipantRobustAgainstNull() {
		cmCodeActionParticipant = new ContentModelCodeActionParticipant();
		cmCodeActionParticipant.doCodeAction(null, null, null, null, null, null, () -> {
		});
		cmCodeActionParticipant.doCodeAction(new Diagnostic(), null, null, null, null, null, () -> {
		});
	}

}