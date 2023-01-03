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
package org.eclipse.lemminx.extensions.references.utils;

import java.util.List;

import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;

/**
 * The XML reference searcher.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLReferencesSearchContext {

	private final List<XMLReferenceExpression> expressions;

	private final boolean searchInAttribute;

	private final boolean searchInText;

	public XMLReferencesSearchContext(List<XMLReferenceExpression> expressions, boolean searchForFromNode) {
		this.expressions = expressions;
		if (searchForFromNode) {
			searchInAttribute = expressions.stream()
					.anyMatch(expr -> expr.isFromSearchInAttribute());
			searchInText = expressions.stream()
					.anyMatch(expr -> expr.isFromSearchInText());
		} else {
			searchInAttribute = expressions.stream()
					.anyMatch(expr -> expr.isToSearchInAttribute());
			searchInText = expressions.stream()
					.anyMatch(expr -> expr.isToSearchInText());
		}
	}

	/**
	 * Returns true if the search of nodes must be done in attribute nodes and false
	 * otherwise.
	 * 
	 * @return true if the search of nodes must be done in attribute nodes and false
	 *         otherwise.
	 */
	public boolean isSearchInAttribute() {
		return searchInAttribute;
	}

	/**
	 * Returns true if the search of nodes must be done in text nodes and false
	 * otherwise.
	 * 
	 * @return true if the search of nodes must be done in text nodes and false
	 *         otherwise.
	 */
	public boolean isSearchInText() {
		return searchInText;
	}

	/**
	 * Returns the list of reference expressions.
	 * 
	 * @return the list of reference expressions.
	 */
	public List<XMLReferenceExpression> getExpressions() {
		return expressions;
	}
}
