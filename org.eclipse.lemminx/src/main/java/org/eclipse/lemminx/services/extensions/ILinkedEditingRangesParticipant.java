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
package org.eclipse.lemminx.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Linked editing ranges participant API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ILinkedEditingRangesParticipant {

	/**
	 * Find linked editing ranges for DOM document and position defined in the given
	 * <code>request</code>.
	 * 
	 * @param request       the linked editing ranges request.
	 * @param ranges        the ranges to update.
	 * @param cancelChecker the cancel checker.
	 */
	void findLinkedEditingRanges(ILinkedEditingRangesRequest request, List<Range> ranges, CancelChecker cancelChecker);

}
