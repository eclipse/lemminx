package toremove.org.eclipse.lsp4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("textDocument")
public interface ExtendedLanguageServer {

	@JsonRequest
	CompletableFuture<List<? extends FoldingRange>> foldingRanges(FoldingRangeRequestParams params);
}
