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
package org.eclipse.lemminx.extensions.relaxng.jing;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.eclipse.lemminx.extensions.relaxng.RelaxNGConstants;

/**
 * All information specific to RelaxNG grammars.
 * 
 * @author Angelo ZERR
 *
 */
public class RelaxNGDescription extends XMLResourceIdentifierImpl implements XMLGrammarDescription {

	public RelaxNGDescription(String systemId, String baseSystemId) {
		super(null, systemId, baseSystemId, getExpandSystemId(systemId, baseSystemId));
	}

	private static String getExpandSystemId(String systemId, String baseSystemId) {
		try {
			return XMLEntityManager.expandSystemId(systemId, baseSystemId, false);
		} catch (Exception e) {
			return systemId;
		}
	}

	@Override
	public String getGrammarType() {
		return RelaxNGConstants.RELAX_NG;
	}

}
