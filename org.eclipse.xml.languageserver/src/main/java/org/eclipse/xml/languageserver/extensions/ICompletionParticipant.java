package org.eclipse.xml.languageserver.extensions;

import org.eclipse.lsp4j.Range;
import org.eclipse.xml.languageserver.services.CompletionContext;

public interface ICompletionParticipant {

	void onAttributeValue(String valuePrefix, Range fullRange, CompletionContext context);

}
