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
package org.eclipse.lemminx.extensions.filepath;

import java.util.List;

import org.eclipse.lemminx.dom.DOMDocument;

/**
 * File path support participant API.
 * 
 * <p>
 * This API provides the capability to contribute to
 * mark some DOM nodes as file type to have file path completion inside the DOM
 * node.
 * </p>
 */
public interface IFilePathSupportParticipant {

	/**
	 * Returns the file path expressions used to mark DOM nodes as file type for the
	 * given DOM document and null or empty otherwise.
	 * 
	 * @param document the DOM document.
	 * 
	 * @return the file path expressions used to mark DOM nodes as file type for the
	 *         given DOM document and null or empty otherwise.
	 */
	List<IFilePathExpression> collectFilePathExpressions(DOMDocument document);
}
