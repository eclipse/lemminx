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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;

/**
 * Link which stores all from and to references for a given expression.
 * 
 * @author Angelo ZERR
 *
 */
public class ReferenceLink {

	private final XMLReferenceExpression expression;

	private final List<SearchNode> froms;
	private final List<SearchNode> tos;

	public ReferenceLink(XMLReferenceExpression expression) {
		this.froms = new ArrayList<>();
		this.tos = new ArrayList<>();
		this.expression = expression;
	}

	public void addTo(SearchNode to) {
		tos.add(to);
	}

	public void addFrom(SearchNode from) {
		froms.add(from);
	}

	public List<SearchNode> getFroms() {
		return froms;
	}

	public List<SearchNode> getTos() {
		return tos;
	}

	public XMLReferenceExpression getExpression() {
		return expression;
	}
}
