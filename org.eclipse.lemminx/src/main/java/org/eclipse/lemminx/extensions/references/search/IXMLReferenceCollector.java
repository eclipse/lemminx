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
package org.eclipse.lemminx.extensions.references.search;

import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;

/**
 * API to collect from/to attribute, text nodes which matches
 * {@link XMLReferenceExpression#matchTo(org.w3c.dom.Node)}
 * 
 * @author Angelo ZERR
 *
 */
@FunctionalInterface
public interface IXMLReferenceCollector {

	/**
	 * Collect the from / to search node which matches the given expression.
	 * 
	 * @param fromSearchNode the from attribute, text node to collect.
	 * @param toSearchNode   the to attribute, text node to collect.
	 * @param expression     the reference expression which matches the from / to
	 *                       node.
	 */
	void collect(SearchNode fromSearchNode, SearchNode toSearchNode, XMLReferenceExpression expression);
}
