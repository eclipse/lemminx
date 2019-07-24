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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.client.ClientCommands;
import org.eclipse.lsp4xml.client.CodeLensKind;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.xsd.utils.XSDUtils;
import org.eclipse.lsp4xml.services.extensions.ICodeLensParticipant;
import org.eclipse.lsp4xml.services.extensions.ICodeLensRequest;
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

		private transient int nbReferences = 1;

		public ReferenceCommand(String uri, Position position, boolean supportedByClient) {
			super(getTitle(1), supportedByClient ? ClientCommands.SHOW_REFERENCES : "");
			super.setArguments(Arrays.asList(uri, position));
		}

		public void increment() {
			nbReferences++;
			super.setTitle(getTitle(nbReferences));
		}

		private static String getTitle(int nbReferences) {
			if (nbReferences == 1) {
				return nbReferences + " reference";
			}
			return nbReferences + " references";
		}

	}

	@Override
	public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
		DOMDocument xmlDocument = request.getDocument();
		// XSD types CodeLens is applicable only for XML Schema file
		if (!DOMUtils.isXSD(xmlDocument)) {
			return;
		}
		boolean supportedByClient = request.isSupportedByClient(CodeLensKind.References);
		// Add references CodeLens for each xs:simpleType, xs:complexType, xs:element,
		// xs:group root element.
		Map<DOMElement, CodeLens> cache = new HashMap<>();
		XSDUtils.searchXSOriginAttributes(xmlDocument, (origin, target) -> {
			// Increment references count Codelens for the given target element
			DOMElement targetElement = target.getOwnerElement();
			CodeLens codeLens = cache.get(targetElement);
			if (codeLens == null) {
				Range range = XMLPositionUtility.createRange(target);
				codeLens = new CodeLens(range);
				codeLens.setCommand(
						new ReferenceCommand(xmlDocument.getDocumentURI(), range.getStart(), supportedByClient));
				cache.put(targetElement, codeLens);
				lenses.add(codeLens);
			} else {
				((ReferenceCommand) codeLens.getCommand()).increment();
			}
		}, cancelChecker);
	}

}
