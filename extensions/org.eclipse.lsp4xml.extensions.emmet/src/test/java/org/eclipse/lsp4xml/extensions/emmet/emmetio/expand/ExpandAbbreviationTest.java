package org.eclipse.lsp4xml.extensions.emmet.emmetio.expand;

import org.eclipse.lsp4xml.extensions.emmet.emmetio.expand.ExpandAbbreviation;
import org.eclipse.lsp4xml.extensions.emmet.emmetio.expand.ExpandOptions;
import org.junit.Assert;
import org.junit.Test;

public class ExpandAbbreviationTest {

	@Test
	public void testExpand() {
		ExpandOptions options = new ExpandOptions();
		options.syntax = "xsl";
		String code = ExpandAbbreviation.expand("a>b", options);
		Assert.assertEquals("<a><b></b></a>", code);
	}
}
