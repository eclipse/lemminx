package toremove.org.eclipse.lsp4j;

/**
 * TODO: remove this class when lsp4j will support foldings
 * https://github.com/eclipse/lsp4j/issues/169
 *
 */
public class FoldingRange {

	/**
	 * The zero-based line number from where the folded range starts.
	 */
	private int startLine;

	/**
	 * The zero-based character offset from where the folded range starts. If not
	 * defined, defaults to the length of the start line.
	 */
	private Integer startCharacter;

	/**
	 * The zero-based line number where the folded range ends.
	 */
	private int endLine;

	/**
	 * The zero-based character offset before the folded range ends. If not defined,
	 * defaults to the length of the end line.
	 */
	private Integer endCharacter;

	/**
	 * Describes the kind of the folding range such as `comment' or 'region'. The
	 * kind is used to categorize folding ranges and used by commands like 'Fold all
	 * comments'. See [FoldingRangeKind](#FoldingRangeKind) for an enumeration of
	 * standardized kinds.
	 */
	private String kind;

	public FoldingRange(int startLine, int endLine) {
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public Integer getStartCharacter() {
		return startCharacter;
	}

	public void setStartCharacter(Integer startCharacter) {
		this.startCharacter = startCharacter;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public Integer getEndCharacter() {
		return endCharacter;
	}

	public void setEndCharacter(Integer endCharacter) {
		this.endCharacter = endCharacter;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

}
