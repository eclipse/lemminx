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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.xsd.utils.XSDUtils;
import org.eclipse.lsp4xml.services.extensions.ICodeLensParticipant;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * XSD CodeLens to show references count between XSD types references.
 * 
 * @author Angelo ZERR
 *
 */
public class XSDCodeLensParticipant implements ICodeLensParticipant {

	static class ReferenceCommand extends Command {

		private transient int nbReferences;

		public ReferenceCommand() {
			nbReferences = 1;
			super.setTitle(nbReferences + " reference");
		}

		public void increment() {
			nbReferences++;
			super.setTitle(nbReferences + " references");
		}

	}

	@Override
	public void doCodeLens(DOMDocument xmlDocument, List<CodeLens> lenses, CancelChecker cancelChecker) {
		// XSD types CodeLens is applicable only for XML Schema file
		if (!DOMUtils.isXSD(xmlDocument)) {
			return;
		}
		// Add references CodeLens for each xs:simpleType, xs:complexType, xs:element,
		// xs:group root element.
		Map<DOMElement, CodeLens> cache = new HashMap<>();
		XSDUtils.searchXSOriginAttributes(xmlDocument, (origin, target) -> {
			// Increment references count Codelens for the given target element
			DOMElement targetElement = target.getOwnerElement();
			CodeLens codeLens = cache.get(targetElement);
			if (codeLens == null) {
				codeLens = new CodeLens(
						XMLPositionUtility.createRange(target.getStart(), target.getEnd(), xmlDocument));
				codeLens.setCommand(new ReferenceCommand());
				cache.put(targetElement, codeLens);
				lenses.add(codeLens);
			} else {
				((ReferenceCommand) codeLens.getCommand()).increment();
			}
		}, cancelChecker);
	}

}
