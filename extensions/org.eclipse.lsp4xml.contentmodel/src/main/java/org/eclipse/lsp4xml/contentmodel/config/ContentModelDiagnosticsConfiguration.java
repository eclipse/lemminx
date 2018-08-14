package org.eclipse.lsp4xml.contentmodel.config;

public class ContentModelDiagnosticsConfiguration {

	private String[] catalogs;
	
	public void setCatalogs(String[] catalogs) {
		this.catalogs = catalogs;
	}
	
	public String[] getCatalogs() {
		return catalogs;
	}
}
