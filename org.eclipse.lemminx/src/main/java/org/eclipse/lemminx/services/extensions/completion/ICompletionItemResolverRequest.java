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
package org.eclipse.lemminx.services.extensions.completion;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.services.extensions.ISharedSettingsRequest;
import org.eclipse.lsp4j.CompletionItem;

/**
 * Represents a request from the client to resolve a given completion item, with
 * information needed to resolve the request attached.
 */
public interface ICompletionItemResolverRequest extends ISharedSettingsRequest, IComponentProvider {

	/**
	 * Returns the unresolved completion item.
	 *
	 * @return the unresolved completion item
	 */
	CompletionItem getUnresolved();

	/**
	 * Returns the id of the participant that can resolve the request.
	 *
	 * @return the id of the participant that can resolve the request
	 */
	String getParticipantId();

	/**
	 * Returns the value of a field from the supplementary data attached to the
	 * request as a string.
	 *
	 * @param fieldName the name of the field to retrieve the data from
	 * @return the value of a field from the supplementary data attached to the
	 *         request as a string
	 */
	String getDataProperty(String fieldName);

	/**
	 * Returns the value of a field from the supplementary data attached to the
	 * request as an integer, or null if the field is not an integer.
	 *
	 * @param fieldName the name of the field to retrieve the data from
	 * @return the value of a field from the supplementary data attached to the
	 *         request as an integer, or null if the field is not an integer
	 */
	Integer getDataPropertyAsInt(String fieldName);

	/**
	 * Returns the value of a field from the supplementary data attached to the
	 * request as an boolean, or null if the field is not an boolean.
	 *
	 * @param fieldName the name of the field to retrieve the data from
	 * @return the value of a field from the supplementary data attached to the
	 *         request as an boolean, or null if the field is not an boolean
	 */
	Boolean getDataPropertyAsBoolean(String participantId);

	/**
	 * Returns the DOM document.
	 *
	 * @return the DOM document
	 */
	DOMDocument getDocument();

	/**
	 * Returns true if the editor supports delayed resolution of documentation and
	 * false otherwise.
	 *
	 * @returns true if the editor supports delayed resolution of documentation and
	 *          false otherwise
	 */
	public boolean isResolveDocumentationSupported();

	/**
	 * Returns true if the editor supports delayed resolution of additionalTextEdits
	 * and
	 * false otherwise.
	 *
	 * @returns true if the editor supports delayed resolution of
	 *          additionalTextEdits and
	 *          false otherwise
	 */
	public boolean isResolveAdditionalTextEditsSupported();

	/**
	 * Returns the completion offset coming from the completion item data and null
	 * otherwise.
	 * 
	 * @return the completion offset coming from the completion item data and null
	 *         otherwise.
	 */
	Integer getCompletionOffset();

}
