/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.xsd.participants;

import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.extensions.xsd.utils.XSDUtils;
import org.eclipse.lsp4xml.services.extensions.AbstractDefinitionParticipant;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * XSD definition which manages teh following definition:
 * 
 * <ul>
 * <li>xs:element/@type -> xs:complexType/@name</li> *
 * <li>xs:extension/@base -> xs:complexType/@name</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class XSDDefinitionParticipant extends AbstractDefinitionParticipant {

	@Override
	protected boolean match(DOMDocument document) {
		return DOMUtils.isXSD(document);
	}

	@Override
	protected void findDefinition(DOMNode node, Position position, int offset, List<LocationLink> locations,
			CancelChecker cancelChecker) {
		// - xs:element/@type -> xs:complexType/@name
		// - xs:extension/@base -> xs:complexType/@name
		DOMAttr attr = node.findAttrAt(offset);
		if (XSDUtils.isBoundToComplexTypes(attr)) {
			XSDUtils.collectComplexTypes(attr, true, (targetNamespacePrefix, targetAttr) -> {
				LocationLink location = XMLPositionUtility.createLocationLink(attr, targetAttr);
				locations.add(location);
			});
		}
	}

}
