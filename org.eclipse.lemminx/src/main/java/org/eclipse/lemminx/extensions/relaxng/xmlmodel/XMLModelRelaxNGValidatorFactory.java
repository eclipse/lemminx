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
package org.eclipse.lemminx.extensions.relaxng.xmlmodel;

import static org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelDeclaration.isApplicableForRelaxNG;

import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelDeclaration;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelValidator;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelValidatorFactory;

/**
 * {@link XMLModelValidatorFactory} for RelaxNG XML and compact syntax.
 * 
 * <code>
 * 
 * <?xml-model href="file.rng" ?>
 * <?xml-model href="file.rnc" ?>
 * 
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class XMLModelRelaxNGValidatorFactory implements XMLModelValidatorFactory {

	@Override
	public XMLModelValidator createValidator(XMLModelDeclaration modelDeclaration) {
		if (isApplicableForRelaxNG(modelDeclaration)) {
			return new XMLModelRelaxNGValidator(modelDeclaration.getHref());
		}
		return null;
	}

}
