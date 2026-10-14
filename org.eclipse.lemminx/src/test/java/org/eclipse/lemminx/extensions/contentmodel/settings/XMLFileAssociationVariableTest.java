/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link XMLFileAssociation} with variable support.
 */
public class XMLFileAssociationVariableTest {

	@Test
	public void matchesWithFileDirnameVariable() {
		URI docUri = getTestPath("project", "data", "test.xml").toUri();
		XMLFileAssociation fa = createAssociation(
				"${fileDirname}/*.xml", "schema.xsd");
		assertTrue(fa.matches(docUri));
	}

	@Test
	public void matchesWithWorkspaceFolderVariable() {
		Path wsFolder = getTestPath("project");
		URI docUri = getTestPath("project", "data", "test.xml").toUri();
		List<String> wsFolders = Collections.singletonList(wsFolder.toUri().toString());

		XMLFileAssociation fa = createAssociation(
				"${workspaceFolder}/data/*.xml", "schema.xsd");
		fa.setWorkspaceFolderUris(wsFolders);

		assertTrue(fa.matches(docUri));
	}

	@Test
	public void matchesWithWorkspaceFolderDoesNotMatchOtherDir() {
		Path wsFolder = getTestPath("project");
		URI docUri = getTestPath("project", "other", "test.xml").toUri();
		List<String> wsFolders = Collections.singletonList(wsFolder.toUri().toString());

		XMLFileAssociation fa = createAssociation(
				"${workspaceFolder}/data/*.xml", "schema.xsd");
		fa.setWorkspaceFolderUris(wsFolders);

		assertFalse(fa.matches(docUri));
	}

	@Test
	public void noVariablesFallsBackToNormalMatching() {
		URI docUri = getTestPath("project", "data", "test.xml").toUri();
		XMLFileAssociation fa = createAssociation("**/*.xml", "schema.xsd");
		assertTrue(fa.matches(docUri));
	}

	@Test
	public void hasPatternVariablesDetection() {
		XMLFileAssociation withVars = createAssociation(
				"${workspaceFolder}/*.xml", "schema.xsd");
		assertTrue(withVars.hasPatternVariables());

		XMLFileAssociation noVars = createAssociation("*.xml", "schema.xsd");
		assertFalse(noVars.hasPatternVariables());
	}

	@Test
	public void hasSystemIdVariablesDetection() {
		XMLFileAssociation withVars = createAssociation(
				"*.xml", "${workspaceFolder}/schema.xsd");
		assertTrue(withVars.hasSystemIdVariables());

		XMLFileAssociation noVars = createAssociation(
				"*.xml", "schema.xsd");
		assertFalse(noVars.hasSystemIdVariables());
	}

	@Test
	public void getResolvedSystemId() {
		Path wsFolder = getTestPath("project");
		URI docUri = getTestPath("project", "data", "test.xml").toUri();
		List<String> wsFolders = Collections.singletonList(wsFolder.toUri().toString());

		XMLFileAssociation fa = createAssociation(
				"*.xml", "${workspaceFolder}/schemas/schema.xsd");
		fa.setWorkspaceFolderUris(wsFolders);

		String resolved = fa.getResolvedSystemId(docUri);
		assertEquals(wsFolder.toString().replace('\\', '/') + "/schemas/schema.xsd", resolved);
	}

	@Test
	public void getResolvedSystemIdNoVariables() {
		URI docUri = getTestPath("project", "test.xml").toUri();
		XMLFileAssociation fa = createAssociation("*.xml", "schema.xsd");

		assertEquals("schema.xsd", fa.getResolvedSystemId(docUri));
	}

	@Test
	public void matchesCachesResolvedPathMatcher() {
		Path wsFolder = getTestPath("project");
		List<String> wsFolders = Collections.singletonList(wsFolder.toUri().toString());

		XMLFileAssociation fa = createAssociation(
				"${workspaceFolder}/data/*.xml", "schema.xsd");
		fa.setWorkspaceFolderUris(wsFolders);

		URI docUri1 = getTestPath("project", "data", "test1.xml").toUri();
		URI docUri2 = getTestPath("project", "data", "test2.xml").toUri();

		// Both should match, second call uses cached PathMatcher
		assertTrue(fa.matches(docUri1));
		assertTrue(fa.matches(docUri2));
	}

	@Test
	public void matchesWithFileDirnameVariablePerDocument() {
		XMLFileAssociation fa = createAssociation(
				"${fileDirname}/*.xml", "schema.xsd");

		URI docInData = getTestPath("project", "data", "test.xml").toUri();
		URI docInOther = getTestPath("project", "other", "test.xml").toUri();

		// Each document matches in its own directory
		assertTrue(fa.matches(docInData));
		assertTrue(fa.matches(docInOther));
	}

	@Test
	public void matchesWithInvalidPatternAfterResolutionDoesNotThrow() {
		URI docUri = getTestPath("project", "data", "test.xml").toUri();
		XMLFileAssociation fa = createAssociation(
				"${fileDirname}/[unclosed", "schema.xsd");
		assertFalse(fa.matches(docUri));
	}

	@Test
	public void matchesWithEmptyResolvedPatternDoesNotMatch() {
		XMLFileAssociation fa = createAssociation("${env:UNLIKELY_VAR_NAME_99999}", "schema.xsd");
		URI docUri = getTestPath("project", "test.xml").toUri();
		assertFalse(fa.matches(docUri));
	}

	// --- Helpers ---

	private static XMLFileAssociation createAssociation(String pattern, String systemId) {
		XMLFileAssociation fa = new XMLFileAssociation();
		fa.setPattern(pattern);
		fa.setSystemId(systemId);
		return fa;
	}

	private static Path getTestPath(String... parts) {
		Path base = Paths.get(System.getProperty("java.io.tmpdir"), "lemminx-test");
		for (String part : parts) {
			base = base.resolve(part);
		}
		return base;
	}
}
