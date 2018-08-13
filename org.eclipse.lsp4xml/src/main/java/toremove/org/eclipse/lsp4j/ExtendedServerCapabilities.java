package toremove.org.eclipse.lsp4j;

import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.xtext.xbase.lib.Pure;

public class ExtendedServerCapabilities extends ServerCapabilities {

	private Boolean foldingRangeProvider;

	@Pure
	public Boolean getFoldingRangeProvider() {
		return this.foldingRangeProvider;
	}

	public void setFoldingRangeProvider(final Boolean foldingRangeProvider) {
		this.foldingRangeProvider = foldingRangeProvider;
	}
}
