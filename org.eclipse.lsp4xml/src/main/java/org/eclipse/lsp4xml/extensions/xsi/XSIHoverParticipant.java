/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.extensions.xsi;

import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.services.extensions.HoverParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;

/**
 * XSIHoverParticipant
 */
public class XSIHoverParticipant extends HoverParticipantAdapter{

	@Override
	public String onAttributeName(IHoverRequest request) throws Exception {

		DOMAttr attribute = (DOMAttr) request.getNode();
		return XSISchemaModel.computeHoverResponse(attribute, request);
	}

	@Override
	public String onAttributeValue(IHoverRequest request) throws Exception {
		return null;
	}
}