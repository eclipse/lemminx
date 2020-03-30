/**
 *  Copyright (c) 2019 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.te;

import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.junit.jupiter.api.Test;

/**
 * Test with DTD completion and cache.
 * 
 * @author Angelo ZERR
 *
 */
public class DTDCompletionWithCacheExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void dtdCache() throws Exception {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use cache on file system
			contentModelManager.setUseCache(true);
		};

		// Copy the svg.dtd in the cache folder
		Path expectedLocation = TEST_WORK_DIRECTORY
				.resolve("cache/http/www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");
		// Download resource in a temporary file
		Files.createDirectories(expectedLocation.getParent());
		Path path = Files.createFile(expectedLocation);

		try (ReadableByteChannel rbc = Channels
				.newChannel(DTDCompletionWithCacheExtensionsTest.class.getResourceAsStream("/dtd/svg.dtd"));
				FileOutputStream fos = new FileOutputStream(path.toFile())) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}

		// Execute completion based on svg.dtd by using the cache manager
		String fileURI = "test.xml";
		String xml = "<?xml version=\"1.0\" standalone=\"no\" ?>\r\n" + //
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\r\n"
				+ //
				"<svg xmlns=\"http://www.w3.org/2000/svg\">\r\n" + //
				"    <animate attributeName=\"foo\">\r\n" + //
				"        |\r\n"; // <- completion";

		XMLLanguageService ls = new XMLLanguageService();
		XMLAssert.testCompletionFor(ls, xml, null, configuration, fileURI, null, true,
				c("desc", te(4, 8, 4, 8, "<desc></desc>"), "desc"));
	}

}
