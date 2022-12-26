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
package org.eclipse.lemminx.extensions.colors.settings;

import org.eclipse.lemminx.xpath.matcher.XPathMatcher;
import org.w3c.dom.Node;

/**
 * XML colors expression
 * 
 * <code>
 * {
     "xpath": "@color"
   }
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class XMLColorExpression {

	private transient XPathMatcher pathMatcher;

	private String xpath;

	public String getXPath() {
		return xpath;
	}

	public void setXPath(String xpath) {
		this.xpath = xpath;
	}

	public boolean match(final Node node) {
		if (xpath == null) {
			return false;
		}
		if (pathMatcher == null) {
			pathMatcher = new XPathMatcher(xpath);
		}
		return pathMatcher.match(node);
	}

}
