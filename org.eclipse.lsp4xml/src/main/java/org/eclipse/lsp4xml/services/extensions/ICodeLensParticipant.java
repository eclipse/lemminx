package org.eclipse.lsp4xml.services.extensions;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public interface ICodeLensParticipant {

	void doCodeLens(ICodeLensRequest request, List<CodeLens> lenses, CancelChecker cancelChecker);

}
