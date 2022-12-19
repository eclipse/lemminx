/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.xinclude;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMElement;

/**
 * XInclude utilities.
 *
 */
public class XIncludeUtils {

	public static final String XINCLUDE_ELT = "xi:include";

	public static final String HREF_ATTR = "href";

	public static final String PARSE_ATTR = "parse";

	public static final String ACCEPT_ATTR = "accept";

	public static final String ACCEPT_LANGUAGE_ATTR = "accept-language";

	public static final String XPOINTER_ATTR = "xpointer";

	public static boolean isInclude(DOMElement element) {
		return element != null && XINCLUDE_ELT.equals(element.getTagName());
	}

	public static DOMAttr getHref(DOMElement element) {
		return element.getAttributeNode(HREF_ATTR);
	}
}
