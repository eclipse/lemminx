package toremove.org.eclipse.lsp4j;

/**
 * TODO: remove this class when lsp4j will support foldings
 * https://github.com/eclipse/lsp4j/issues/169
 *
 */

/**
 * Enum of known range kinds
 */
public final class FoldingRangeKind {

	/**
	 * Folding range for a comment
	 */
	public static final String Comment = "comment";
	/**
	 * Folding range for a imports or includes
	 */
	public static final String Imports = "imports";
	/**
	 * Folding range for a region (e.g. `#region`)
	 */
	public static final String Region = "region";
}