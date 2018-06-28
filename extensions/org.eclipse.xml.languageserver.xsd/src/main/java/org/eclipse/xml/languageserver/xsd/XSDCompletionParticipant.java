package org.eclipse.xml.languageserver.xsd;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Range;
import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;
import org.eclipse.xml.languageserver.extensions.ICompletionRequest;
import org.eclipse.xml.languageserver.extensions.ICompletionResponse;

public class XSDCompletionParticipant implements ICompletionParticipant {

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) {
		response.addCompletionItem(new CompletionItem("AbcA"));
		response.addCompletionItem(new CompletionItem("BcBB"));
	}

}
