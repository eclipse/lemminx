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

import static org.eclipse.lemminx.extensions.references.search.SearchQueryFactory.getInversedDirection;

import java.util.List;

import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.references.search.SearchNode.Direction;
import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;

/**
 * XML references search Query.
 * 
 * @author Angelo ZERR
 *
 */
public class SearchQuery {

	public static enum QueryDirection {
		FROM_2_TO, TO_2_FROM, BOTH;
	}

	private final DOMNode node;
	private final List<XMLReferenceExpression> expressions;
	private final QueryDirection queryDirection;
	private boolean searchInAttribute;
	private boolean searchInText;
	private boolean matchNode;
	private boolean searchInIncludedFiles;
	private SearchNode searchNode;

	public SearchQuery(DOMNode node, int offset, List<XMLReferenceExpression> expressions, QueryDirection direction) {
		this(node, offset, null, expressions, direction);
	}

	public SearchQuery(SearchNode searchNode, List<XMLReferenceExpression> expressions, QueryDirection direction) {
		this(searchNode.getNode(), -1, searchNode, expressions, direction);
	}

	private SearchQuery(DOMNode node, int offset, SearchNode searchNode, List<XMLReferenceExpression> expressions,
			QueryDirection queryDirection) {
		this.node = node;
		this.expressions = expressions;
		this.queryDirection = queryDirection;
		searchInAttribute = false;
		searchInText = false;
		boolean hasMultiple = false;
		String prefix = null;
		Direction searchNodeDirection = getSearchNodeDirection(offset, queryDirection);
		for (XMLReferenceExpression expression : expressions) {
			if (!searchInAttribute) {
				searchInAttribute = isSearchInAttribute(expression, queryDirection);
			}
			if (!searchInText) {
				searchInText = isSearchInText(expression, queryDirection);
			}
			if (!hasMultiple) {
				hasMultiple = expression.isMultiple();
			}
			if (prefix == null) {
				prefix = expression.getPrefix();
			}
			if (searchNodeDirection == null) {
				Direction status = getInversedDirection(node, expression, queryDirection);
				if (status != null) {
					searchNodeDirection = status;
				}
			}
		}
		if (searchNode != null) {
			this.searchNode = searchNode;
		} else if (offset != -1) {
			this.searchNode = SearchNodeFactory.getSearchNodeAt(node, offset, prefix, hasMultiple, searchNodeDirection);
		}
	}

	private Direction getSearchNodeDirection(int offset, QueryDirection queryDirection) {
		switch (queryDirection) {
			case FROM_2_TO:
				return Direction.FROM;
			case TO_2_FROM:
				return Direction.TO;
			default:
				if (offset != -1) {
					return null;
				}
				return null;
		}
	}

	public DOMNode getNode() {
		return node;
	}

	public List<XMLReferenceExpression> getExpressions() {
		return expressions;
	}

	public boolean isMatchNode() {
		return matchNode;
	}

	public boolean isSearchInIncludedFiles() {
		return searchInIncludedFiles;
	}

	public void setSearchInIncludedFiles(boolean searchInIncludedFiles) {
		this.searchInIncludedFiles = searchInIncludedFiles;
	}

	public void setMatchNode(boolean matchNode) {
		this.matchNode = matchNode;
	}

	public boolean isSearchInAttribute() {
		return searchInAttribute;
	}

	public boolean isSearchInText() {
		return searchInText;
	}

	public SearchNode getSearchNode() {
		return searchNode;
	}

	public QueryDirection getQueryDirection() {
		return queryDirection;
	}

	private static boolean isSearchInAttribute(XMLReferenceExpression expression, QueryDirection direction) {
		switch (direction) {
			case FROM_2_TO:
				return expression.isToSearchInAttribute();
			case TO_2_FROM:
				return expression.isFromSearchInAttribute();
			default:
				return expression.isToSearchInAttribute() || expression.isFromSearchInAttribute();
		}
	}

	private static boolean isSearchInText(XMLReferenceExpression expression, QueryDirection direction) {
		switch (direction) {
			case FROM_2_TO:
				return expression.isToSearchInText();
			case TO_2_FROM:
				return expression.isFromSearchInText();
			default:
				return expression.isToSearchInText() || expression.isFromSearchInText();
		}
	}

}
