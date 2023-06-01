/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions.rename;

import java.util.List;

import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;

/**
 * Rename response API.
 *
 * @dince 0.26
 */
public interface IRenameResponse {

	/**
	 * Adds text document edit to rename response.
	 * 
	 * @param request A rename request
	 * @param textDocumentEdit A TextDocumentEdit object to be added to rename response
	 * @since 0.26
	 */
	void addTextDocumentEdit(TextDocumentEdit textDocumentEdit);

	/**
	 * Adds resource operation to rename response.
	 * 
	 * @param request A rename request
	 * @param resourceOperation A ResourceOperation object to store to rename response
	 * @since 0.26
	 */
	void addResourceOperation(ResourceOperation resourceOperation);

}