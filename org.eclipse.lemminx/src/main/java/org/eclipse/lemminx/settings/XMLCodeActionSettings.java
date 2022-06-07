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
package org.eclipse.lemminx.settings;

import org.eclipse.lsp4j.CodeActionCapabilities;

/**
 * A wrapper around LSP {@link CodeActionCapabilities}.
 *
 */
public class XMLCodeActionSettings {

	private CodeActionCapabilities capabilities;

	public void setCapabilities(CodeActionCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public CodeActionCapabilities getCapabilities() {
		return capabilities;
	}

	public boolean canSupportResolve() {
		return capabilities != null && capabilities.getResolveSupport() != null;
	}

}
