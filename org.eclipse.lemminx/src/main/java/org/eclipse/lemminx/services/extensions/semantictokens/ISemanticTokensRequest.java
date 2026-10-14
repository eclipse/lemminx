/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.semantictokens;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IComponentProvider;

/**
 * Semantic tokens request API.
 */
public interface ISemanticTokensRequest extends IComponentProvider {

	/**
	 * Returns the DOM document.
	 *
	 * @return the DOM document.
	 */
	DOMDocument getDocument();
}
