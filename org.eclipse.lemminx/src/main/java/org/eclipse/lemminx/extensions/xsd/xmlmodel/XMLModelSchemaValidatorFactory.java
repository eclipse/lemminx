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
package org.eclipse.lemminx.extensions.xsd.xmlmodel;

import static org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelDeclaration.isApplicableForXSD;

import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelDeclaration;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelValidator;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelValidatorFactory;

/**
 * {@link XMLModelValidatorFactory} for XSD.
 * 
 * <code>
 * 
 * <?xml-model href="file.xsd" ?>
 * 
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class XMLModelSchemaValidatorFactory implements XMLModelValidatorFactory {

	@Override
	public XMLModelValidator createValidator(XMLModelDeclaration modelDeclaration) {
		if (isApplicableForXSD(modelDeclaration)) {
			return new XMLModelSchemaValidator(modelDeclaration.getHref());
		}
		return null;
	}

}
