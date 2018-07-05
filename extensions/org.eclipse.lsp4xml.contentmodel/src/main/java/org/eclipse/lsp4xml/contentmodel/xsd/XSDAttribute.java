/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.xsd;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.eclipse.lsp4xml.contentmodel.CMAttribute;

/**
 * XSD attribute declaration implementation.
 *
 */
public class XSDAttribute implements CMAttribute {

	private final XSAttributeDeclaration attributeDeclaration;

	public XSDAttribute(XSAttributeDeclaration attributeDeclaration) {
		this.attributeDeclaration = attributeDeclaration;
	}

	@Override
	public String getName() {
		return attributeDeclaration.getName();
	}

}
