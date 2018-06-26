package org.eclipse.xml.languageserver.xsd;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Range;
import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;
import org.eclipse.xml.languageserver.services.CompletionContext;

public class XSDCompletionParticipant implements ICompletionParticipant {

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, CompletionContext context) {
		context.addItem(new CompletionItem("AbcA"));
		context.addItem(new CompletionItem("BBBB"));
	}

}
