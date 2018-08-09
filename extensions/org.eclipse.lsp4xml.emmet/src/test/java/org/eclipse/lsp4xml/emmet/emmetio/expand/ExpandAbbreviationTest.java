package org.eclipse.lsp4xml.emmet.emmetio.expand;

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
