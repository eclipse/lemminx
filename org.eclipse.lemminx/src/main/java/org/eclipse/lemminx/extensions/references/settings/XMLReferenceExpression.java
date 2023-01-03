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

import org.eclipse.lemminx.xpath.matcher.IXPathNodeMatcher.MatcherType;
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

	/**
	 * Returns true if the given DOM Node match the XPath expression of the 'from'
	 * XPath matcher and false otherwise.
	 * 
	 * @param node the DOM Node to match.
	 * 
	 * @return true if the given DOM Node match the XPath expression of the 'from'
	 *         XPath matcher and false otherwise.
	 */
	public boolean matchFrom(final Node node) {
		return getFromMatcher().match(node);
	}

	private XPathMatcher getFromMatcher() {
		if (fromMatcher == null) {
			fromMatcher = new XPathMatcher(from);
		}
		return fromMatcher;
	}

	/**
	 * Returns true if the given DOM Node match the XPath expression of the 'to'
	 * XPath matcher and false otherwise.
	 * 
	 * @param node the DOM Node to match.
	 * 
	 * @return true if the given DOM Node match the XPath expression of the 'to'
	 *         XPath matcher and false otherwise.
	 */
	public boolean matchTo(final Node node) {
		return getToMatcher().match(node);
	}

	private XPathMatcher getToMatcher() {
		if (toMatcher == null) {
			toMatcher = new XPathMatcher(to);
		}
		return toMatcher;
	}

	public boolean isFromSearchInAttribute() {
		return getFromMatcher().getNodeSelectorType() == MatcherType.ATTRIBUTE;
	}

	public boolean isFromSearchInText() {
		return getFromMatcher().getNodeSelectorType() == MatcherType.TEXT;
	}

	public boolean isToSearchInAttribute() {
		return getToMatcher().getNodeSelectorType() == MatcherType.ATTRIBUTE;
	}

	public boolean isToSearchInText() {
		return getToMatcher().getNodeSelectorType() == MatcherType.TEXT;
	}
}
