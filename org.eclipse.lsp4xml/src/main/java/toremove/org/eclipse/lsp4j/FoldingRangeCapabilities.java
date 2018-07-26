package toremove.org.eclipse.lsp4j;

/**
 * TODO: remove this class when lsp4j will support foldings
 * https://github.com/eclipse/lsp4j/issues/169
 *
 */
public class FoldingRangeCapabilities {

	/**
	 * The maximum number of folding ranges that the client prefers to receive per document. The value serves as a
	 * hint, servers are free to follow the limit.
	 */
	Integer rangeLimit;
	
	public void setRangeLimit(Integer rangeLimit) {
		this.rangeLimit = rangeLimit;
	}
	
	public Integer getRangeLimit() {
		return rangeLimit;
	}
}
