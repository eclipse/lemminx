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

import static org.eclipse.lemminx.XMLAssert.ll;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testTypeDefinitionFor;

import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lemminx.services.XMLLanguageService;
import org.junit.jupiter.api.Test;

/**
 * Test with XML Schema type definition and cache.
 * 
 * @author Angelo ZERR
 *
 */
public class XMLSchemaTypeDefinitionWithCacheExtensionsTest extends AbstractCacheBasedTest {

	@Test
	public void typeDefinitionPOMCache() throws Exception {
		Consumer<XMLLanguageService> configuration = ls -> {
			ContentModelManager contentModelManager = ls.getComponent(ContentModelManager.class);
			// Use cache on file system
			contentModelManager.setUseCache(true);
		};

		// Copy the svg.dtd in the cache folder
		Path expectedLocation = TEST_WORK_DIRECTORY.resolve("cache/http/maven.apache.org/xsd/maven-4.0.0.xsd");
		// Download resource in a temporary file
		Files.createDirectories(expectedLocation.getParent());
		Path path = Files.createFile(expectedLocation);

		try (ReadableByteChannel rbc = Channels.newChannel(
				XMLSchemaTypeDefinitionWithCacheExtensionsTest.class.getResourceAsStream("/xsd/maven-4.0.0.xsd"));
				FileOutputStream fos = new FileOutputStream(path.toFile())) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}

		XMLLanguageService ls = new XMLLanguageService();
		String targetSchemaURI = expectedLocation.toUri().toString();

		// Execute type definition based on maven-4.0.0.xsd by using the cache manager

		// /project type definition
		String xml = "<proj|ect xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + // <- type definition for project
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n";

		testTypeDefinitionFor(ls, null, configuration, xml, "pom.xml",
				ll(targetSchemaURI, r(0, 1, 0, 8), r(6, 19, 6, 28)));

		// /project/parent type definition
		xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + // <- type definition for project
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ "<paren|t>";

		testTypeDefinitionFor(ls, null, configuration, xml, "pom.xml",
				ll(targetSchemaURI, r(3, 1, 3, 7), r(34, 37, 34, 45)));

		// /project/parent/groupId type definition
		xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + // <- type definition for project
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ "<parent>" + "<grou|pId>";

		testTypeDefinitionFor(ls, null, configuration, xml, "pom.xml",
				ll(targetSchemaURI, r(3, 9, 3, 16), r(358, 37, 358, 46)));

		// /project/XXX type definition
		xml = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + // <- type definition for project
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"
				+ "<XX|X>";

		testTypeDefinitionFor(ls, null, configuration, xml, "pom.xml");
	}

}
