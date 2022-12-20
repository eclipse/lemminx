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

import org.eclipse.lemminx.xpath.matcher.XPathMatcher;
import org.w3c.dom.Node;

/**
 * XML reference expression
 * 
 * <code>
 * {
     "prefix": "#",
     "from": "@corresp",
     "to": "@xml:id"
   }
 * </code>
 * 
 * @author azerr
 *
 */
public class XMLReferenceExpression {

	private transient XPathMatcher fromMatcher;

	private transient XPathMatcher toMatcher;

	private String prefix;

	private String from;

	private String to;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean matchFrom(final Node node) {
		if (fromMatcher == null) {
			fromMatcher = new XPathMatcher(from);
		}
		return fromMatcher.match(node);
	}

	public boolean matchTo(final Node node) {
		if (toMatcher == null) {
			toMatcher = new XPathMatcher(to);
		}
		return toMatcher.match(node);
	}
}
