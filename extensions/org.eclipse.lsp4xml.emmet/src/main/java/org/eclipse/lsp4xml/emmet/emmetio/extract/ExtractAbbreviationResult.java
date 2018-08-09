package org.eclipse.lsp4xml.emmet.emmetio.extract;

public class ExtractAbbreviationResult {

	private final String abbreviation;

	private final int location;

	private final int start;

	private final int end;

	public ExtractAbbreviationResult(String abbreviation, int location, int start, int end) {
		super();
		this.abbreviation = abbreviation;
		this.location = location;
		this.start = start;
		this.end = end;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public int getLocation() {
		return location;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

}
