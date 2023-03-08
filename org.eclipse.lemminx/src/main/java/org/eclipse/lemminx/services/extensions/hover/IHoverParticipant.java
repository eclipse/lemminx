/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.services.extensions.hover;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Hover participant API.
 *
 */
public interface IHoverParticipant {

	/**
	 * onTag method
	 *
	 * @param hoverRequest the hover request.
	 * @param the          cancel checker.
	 * 
	 * @return the Value of MarkupContent {@link String}
	 */
	Hover onTag(IHoverRequest request, CancelChecker cancelChecker) throws Exception;

	/**
	 * onAttributeName method
	 *
	 * @param hoverRequest the hover request.
	 * @param the          cancel checker.
	 * 
	 * @return the Value of MarkupContent {@link String}
	 */
	Hover onAttributeName(IHoverRequest request, CancelChecker cancelChecker) throws Exception;

	/**
	 * onAttributeValue method
	 *
	 * @param hoverRequest the hover request.
	 * @param the          cancel checker.
	 * 
	 * @return the Value of MarkupContent {@link String}
	 */
	Hover onAttributeValue(IHoverRequest request, CancelChecker cancelChecker) throws Exception;

	/**
	 * onText method
	 *
	 * @param hoverRequest the hover request.
	 * @param the          cancel checker.
	 * 
	 * @return the Value of MarkupContent {@link String}
	 */
	Hover onText(IHoverRequest request, CancelChecker cancelChecker) throws Exception;

}
