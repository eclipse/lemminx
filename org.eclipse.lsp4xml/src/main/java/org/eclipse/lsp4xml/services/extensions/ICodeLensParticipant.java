package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;

public interface ICodeLensParticipant {

	void doCodeLens(DOMDocument xmlDocument, List<CodeLens> lenses, CancelChecker cancelChecker);

}
