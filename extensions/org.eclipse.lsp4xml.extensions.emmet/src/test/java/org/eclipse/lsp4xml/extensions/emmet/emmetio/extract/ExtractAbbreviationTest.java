package org.eclipse.lsp4xml.extensions.emmet.emmetio.extract;

import org.eclipse.lsp4xml.extensions.emmet.emmetio.extract.ExtractAbbreviation;
import org.eclipse.lsp4xml.extensions.emmet.emmetio.extract.ExtractAbbreviationResult;
import org.junit.Assert;
import org.junit.Test;

public class ExtractAbbreviationTest {

	@Test
	public void testExtract() throws Exception {
		ExtractAbbreviationResult e = ExtractAbbreviation.extractAbbreviation("a>b", null, null);
		Assert.assertEquals("a>b", e.getAbbreviation());
	}
}
