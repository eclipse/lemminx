package org.eclipse.lsp4xml.emmet.emmetio.extract;

public class EmmetOptions {

	private String syntax;

	private Boolean lookAhead;

	private String prefix;

	public String getSyntax() {
		return syntax;
	}

	public EmmetOptions setSyntax(String syntax) {
		this.syntax = syntax;
		return this;
	}

	public Boolean getLookAhead() {
		return lookAhead;
	}

	public EmmetOptions setLookAhead(Boolean lookAhead) {
		this.lookAhead = lookAhead;
		return this;
	}

	public String getPrefix() {
		return prefix;
	}

	public EmmetOptions setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

}
