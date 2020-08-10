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
package org.eclipse.lemminx.extensions.xerces.xmlmodel;

import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.XMLValidator;

/**
 * {@link XMLValidator} factory API.
 * 
 * @author Angelo ZERR
 *
 */
public interface XMLModelValidatorFactory {

	/**
	 * Create an {@link XMLModelValidator} instance if the XML model declaration is
	 * applicable for the factory and null otherwise.
	 * 
	 * @param modelDeclaration the XML model declaration which host href, type,
	 *                         shematypens.
	 * 
	 * @return an {@link XMLModelValidator} instance if the XML model declaration is
	 *         applicable for the factory and null otherwise.
	 */
	XMLModelValidator createValidator(XMLModelDeclaration modelDeclaration);

}
