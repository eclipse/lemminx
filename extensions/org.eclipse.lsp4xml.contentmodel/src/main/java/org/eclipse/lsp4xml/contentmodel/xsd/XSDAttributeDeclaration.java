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

import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.CMAttributeDeclaration;

/**
 * XSD attribute declaration implementation.
 *
 */
public class XSDAttributeDeclaration implements CMAttributeDeclaration {

	private final XSAttributeDeclaration attributeDeclaration;

	public XSDAttributeDeclaration(XSAttributeDeclaration attributeDeclaration) {
		this.attributeDeclaration = attributeDeclaration;
	}

	@Override
	public String getName() {
		return attributeDeclaration.getName();
	}

	@Override
	public String getDocumentation() {
		XSAnnotation annotation = attributeDeclaration.getAnnotation();
		return annotation != null ? annotation.getAnnotationString() : null;
	}
}
