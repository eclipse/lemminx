package org.eclipse.lsp4xml.extensions;

import org.eclipse.lsp4j.Hover;

public interface IHoverParticipant {

	Hover onTag(IHoverRequest request);
	
}
