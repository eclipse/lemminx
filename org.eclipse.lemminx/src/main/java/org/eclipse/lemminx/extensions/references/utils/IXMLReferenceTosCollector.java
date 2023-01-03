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
package org.eclipse.lemminx.extensions.references.utils;

import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;

/**
 * API to collect to attribute which matches
 * {@link XMLReferenceExpression#matchTo(org.w3c.dom.Node)}
 * 
 * @author Angelo ZERR
 *
 */
@FunctionalInterface
public interface IXMLReferenceTosCollector {

	/**
	 * Collect the given to attribute which matches the given expression.
	 * 
	 * @param namespacePrefix namespace prefix.
	 * @param toNode          the to attribute, text node to collect.
	 * @param expression      the reference expression which matches the to
	 *                        node.
	 */
	void collect(String namespacePrefix, DOMNode toNode, XMLReferenceExpression expression);
}
