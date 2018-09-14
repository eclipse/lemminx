package org.eclipse.lsp4xml.dom;

public class LineIndentInfo {

	private final String lineDelimiter;
	private final String whitespacesIndent;

	public LineIndentInfo(String lineDelimiter, String whitespacesIndent) {
		this.lineDelimiter = lineDelimiter;
		this.whitespacesIndent = whitespacesIndent;
	}
	
	public String getLineDelimiter() {
		return lineDelimiter;
	}
	
	public String getWhitespacesIndent() {
		return whitespacesIndent;
	}

}
