/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.settings;

import org.eclipse.lsp4j.HoverCapabilities;

/**
 * A wrapper around LSP {@link HoverCapabilities}.
 *
 */
public class XMLHoverSettings {

	private HoverCapabilities capabilities;

	public void setCapabilities(HoverCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public HoverCapabilities getCapabilities() {
		return capabilities;
	}

}
