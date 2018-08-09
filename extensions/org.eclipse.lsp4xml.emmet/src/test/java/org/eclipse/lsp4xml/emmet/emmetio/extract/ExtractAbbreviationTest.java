package org.eclipse.lsp4xml.emmet.emmetio.extract;

import org.junit.Assert;
import org.junit.Test;

public class ExtractAbbreviationTest {

	@Test
	public void testExtract() throws Exception {
		ExtractAbbreviationResult e = ExtractAbbreviation.extractAbbreviation("a>b", null, null);
		Assert.assertEquals("a>b", e.getAbbreviation());
	}
}
