/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.commons.snippets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

/**
 * Snippet registry test.
 *
 */
public class SnippetRegistryTest {

	@Test
	public void withoutLabel() throws IOException {
		String json = "{\r\n" + //
				"  \"Insert CDATA\": {\r\n" + //
				"    \"prefix\": [\r\n" + //
				"      \"<![CDATA[\"\r\n" + //
				"    ],\r\n" + //
				"    \"description\": \"Insert CDATA\"\r\n" + //
				"  }\r\n" + //
				" }";
		SnippetRegistry registry = new SnippetRegistry(null, false);
		registry.registerSnippets(new StringReader(json));
		assertEquals(1, registry.getSnippets().size());
		// By default label is equals to prefix
		assertEquals("<![CDATA[", registry.getSnippets().get(0).getLabel());
	}

	@Test
	public void staticLabel() throws IOException {
		String json = "{\r\n" + //
				"  \"Insert CDATA\": {\r\n" + //
				"    \"prefix\": [\r\n" + //
				"      \"<![CDATA[\"\r\n" + //
				"    ],\r\n" + //
				"    \"label\": \"Static label\",\r\n" + // <-- label reference description and prefix
				"    \"description\": \"Insert CDATA\"\r\n" + //
				"  }\r\n" + //
				" }";
		SnippetRegistry registry = new SnippetRegistry(null, false);
		registry.registerSnippets(new StringReader(json));
		assertEquals(1, registry.getSnippets().size());
		assertEquals("Static label", registry.getSnippets().get(0).getLabel());
	}

	@Test
	public void withDynamicLabel() throws IOException {
		String json = "{\r\n" + //
				"  \"Insert CDATA\": {\r\n" + //
				"    \"prefix\": [\r\n" + //
				"      \"<![CDATA[\"\r\n" + //
				"    ],\r\n" + //
				"    \"label\": \"$description - $prefix\",\r\n" + // <-- label reference description and prefix
				"    \"description\": \"Insert CDATA\"\r\n" + //
				"  }\r\n" + //
				" }";
		SnippetRegistry registry = new SnippetRegistry(null, false);
		registry.registerSnippets(new StringReader(json));
		assertEquals(1, registry.getSnippets().size());
		assertEquals("Insert CDATA - <![CDATA[", registry.getSnippets().get(0).getLabel());
	}

	@Test
	public void withDynamicLabelAndWithoutDescription() throws IOException {
		String json = "{\r\n" + //
				"  \"Insert CDATA\": {\r\n" + //
				"    \"prefix\": [\r\n" + //
				"      \"<![CDATA[\"\r\n" + //
				"    ],\r\n" + //
				"    \"label\": \"$description - $prefix\"\r\n" + // <-- label reference description and prefix
				// " \"description\": \"Insert CDATA\"\r\n" + //
				"  }\r\n" + //
				" }";
		SnippetRegistry registry = new SnippetRegistry(null, false);
		registry.registerSnippets(new StringReader(json));
		assertEquals(1, registry.getSnippets().size());
		assertEquals("$description - <![CDATA[", registry.getSnippets().get(0).getLabel());
	}

}
