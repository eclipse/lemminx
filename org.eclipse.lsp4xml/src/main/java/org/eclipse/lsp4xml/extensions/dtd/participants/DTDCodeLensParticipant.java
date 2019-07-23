package org.eclipse.lsp4xml.extensions.dtd.participants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.client.CodeLensKind;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DTDDeclNode;
import org.eclipse.lsp4xml.extensions.dtd.utils.DTDUtils;
import org.eclipse.lsp4xml.services.extensions.ICodeLensParticipant;
import org.eclipse.lsp4xml.services.extensions.ICodeLensRequest;
import org.eclipse.lsp4xml.services.extensions.ReferenceCommand;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

public class DTDCodeLensParticipant implements ICodeLensParticipant {

	@Override
	public void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker) {
		DOMDocument xmlDocument = request.getDocument();
		// XSD types CodeLens is applicable only for XML Schema file
		if (!DOMUtils.isDTD(xmlDocument.getDocumentURI()) && !xmlDocument.hasDTD()) {
			return;
		}
		boolean supportedByClient = request.isSupportedByClient(CodeLensKind.References);
		// Add references CodeLens for each xs:simpleType, xs:complexType, xs:element,
		// xs:group root element.
		Map<DTDDeclNode, CodeLens> cache = new HashMap<>();
		DTDUtils.searchDTDOriginElementDecls(xmlDocument.getDoctype(), (origin, target) -> {
			// Increment references count Codelens for the given target element
			DTDDeclNode targetElement = target.getOwnerNode();
			if (targetElement.isDTDElementDecl()) {
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
			}
		}, cancelChecker);
	}

}
