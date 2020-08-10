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

import org.eclipse.lemminx.extensions.relaxng.xml.validator.ExternalRelaxNGValidator;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.XMLModelValidator;

/**
 * XML model validator which process validation with RelaxNG:
 * 
 * <pre>
 * 	&lt;?xml-model href="http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_lite.rng"?&gt;
 * </pre>
 *
 */
public class XMLModelRelaxNGValidator extends ExternalRelaxNGValidator implements XMLModelValidator {

	public XMLModelRelaxNGValidator(String href) {
		super.setExternalRelaxNG(href);
	}

	@Override
	public void setExternalRelaxNG(String externalRelaxNG) {

	}

}
