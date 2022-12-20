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
package org.eclipse.lemminx.extensions.references.settings;

import java.util.List;

import org.eclipse.lemminx.settings.PathPatternMatcher;

/**
 * XML references which stores list of {@link XMLReferenceExpression} applied
 * for a give pattern.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferences extends PathPatternMatcher {

	private List<XMLReferenceExpression> expressions;

	/**
	 * Returns list of XML reference expressions.
	 * 
	 * @return list of XML reference expressions.
	 */
	public List<XMLReferenceExpression> getExpressions() {
		return expressions;
	}

	/**
	 * Set list of XML reference expressions.
	 * 
	 * @param expressions list of XML reference expressions.
	 */
	public void setExpressions(List<XMLReferenceExpression> expressions) {
		this.expressions = expressions;
	}
}
