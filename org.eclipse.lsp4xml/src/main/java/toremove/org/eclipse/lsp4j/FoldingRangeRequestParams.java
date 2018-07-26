package toremove.org.eclipse.lsp4j;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * TODO: remove this class when lsp4j will support foldings
 * https://github.com/eclipse/lsp4j/issues/169
 *
 */
public class FoldingRangeRequestParams {

	@NonNull
	private TextDocumentIdentifier textDocument;

	public FoldingRangeRequestParams() {
	}

	public FoldingRangeRequestParams(@NonNull final TextDocumentIdentifier textDocument) {
		this.textDocument = textDocument;
	}

	@Pure
	@NonNull
	public TextDocumentIdentifier getTextDocument() {
		return this.textDocument;
	}

	public void setTextDocument(@NonNull final TextDocumentIdentifier textDocument) {
		this.textDocument = textDocument;
	}

	@Override
	@Pure
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("textDocument", this.textDocument);
		return b.toString();
	}

	@Override
	@Pure
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FoldingRangeRequestParams other = (FoldingRangeRequestParams) obj;
		if (this.textDocument == null) {
			if (other.textDocument != null)
				return false;
		} else if (!this.textDocument.equals(other.textDocument))
			return false;
		return true;
	}

	@Override
	@Pure
	public int hashCode() {
		return 31 * 1 + ((this.textDocument == null) ? 0 : this.textDocument.hashCode());
	}

}
