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
package org.eclipse.lemminx.settings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PathPatternMatcher}.
 */
public class PathPatternMatcherTest {

	// --- Single wildcard '*' patterns (issue #1643) ---

	@Test
	public void singleWildcardSuffix() {
		// See https://github.com/eclipse-lemminx/lemminx/issues/1643
		PathPatternMatcher matcher = createMatcher("*-suffix.xml");
		assertTrue(matcher.matches("file:///home/user/project/file-suffix.xml"));
		assertTrue(matcher.matches("file:///test/my-suffix.xml"));
		assertTrue(matcher.matches("file:///a/b/c/d/e/deep-suffix.xml"));
		assertFalse(matcher.matches("file:///test/file.xml"));
		assertFalse(matcher.matches("file:///test/file-suffix.json"));
	}

	@Test
	public void singleWildcardExtension() {
		PathPatternMatcher matcher = createMatcher("*.xml");
		assertTrue(matcher.matches("file:///home/user/project/test.xml"));
		assertTrue(matcher.matches("file:///a/b/c/d/test.xml"));
		assertFalse(matcher.matches("file:///test.json"));
		assertFalse(matcher.matches("file:///home/user/test.txt"));
	}

	@Test
	public void singleWildcardPrefix() {
		PathPatternMatcher matcher = createMatcher("pom.*");
		assertTrue(matcher.matches("file:///home/user/project/pom.xml"));
		assertTrue(matcher.matches("file:///test/pom.json"));
		assertFalse(matcher.matches("file:///test/other.xml"));
	}

	@Test
	public void singleWildcardOnly() {
		PathPatternMatcher matcher = createMatcher("*");
		assertTrue(matcher.matches("file:///home/user/project/anything.xml"));
		assertTrue(matcher.matches("file:///test/file.txt"));
	}

	@Test
	public void singleWildcardWithMultipleDots() {
		PathPatternMatcher matcher = createMatcher("*.Format.ps1xml");
		assertTrue(matcher.matches("file:///test/Test.Format.ps1xml"));
		assertTrue(matcher.matches("file:///home/user/My.Format.ps1xml"));
		assertFalse(matcher.matches("file:///test/Test.Types.ps1xml"));
	}

	@Test
	public void singleWildcardInMiddle() {
		PathPatternMatcher matcher = createMatcher("test*file.xml");
		assertTrue(matcher.matches("file:///home/user/test-file.xml"));
		assertTrue(matcher.matches("file:///test/testABCfile.xml"));
		assertTrue(matcher.matches("file:///test/testfile.xml"));
		assertFalse(matcher.matches("file:///test/other.xml"));
	}

	@Test
	public void multipleWildcardsInPattern() {
		PathPatternMatcher matcher = createMatcher("*test*.xml");
		assertTrue(matcher.matches("file:///home/user/mytest1.xml"));
		assertTrue(matcher.matches("file:///test/test.xml"));
		assertTrue(matcher.matches("file:///test/AAtestBB.xml"));
		assertFalse(matcher.matches("file:///test/other.xml"));
	}

	// --- Double wildcard '**' patterns ---

	@Test
	public void doubleWildcardSlashPattern() {
		PathPatternMatcher matcher = createMatcher("**/*-suffix.xml");
		assertTrue(matcher.matches("file:///home/user/project/file-suffix.xml"));
		assertTrue(matcher.matches("file:///test/my-suffix.xml"));
		assertFalse(matcher.matches("file:///test/file.xml"));
	}

	@Test
	public void doubleWildcardWithDirectoryPattern() {
		PathPatternMatcher matcher = createMatcher("**/test/*.xml");
		assertTrue(matcher.matches("file:///home/user/test/file.xml"));
		assertFalse(matcher.matches("file:///home/user/prod/file.xml"));
	}

	@Test
	public void doubleWildcardWithoutSlash() {
		// Workaround pattern from issue #1643
		PathPatternMatcher matcher = createMatcher("**-suffix.xml");
		assertTrue(matcher.matches("file:///home/user/project/file-suffix.xml"));
	}

	@Test
	public void doubleWildcardExtension() {
		PathPatternMatcher matcher = createMatcher("**/*.xml");
		assertTrue(matcher.matches("file:///home/user/project/test.xml"));
		assertTrue(matcher.matches("file:///a/b/c/test.xml"));
	}

	@Test
	public void doubleWildcardWithFileName() {
		PathPatternMatcher matcher = createMatcher("**/pom.xml");
		assertTrue(matcher.matches("file:///home/user/project/pom.xml"));
		assertTrue(matcher.matches("file:///test/pom.xml"));
		assertFalse(matcher.matches("file:///test/other.xml"));
	}

	// --- Question mark '?' patterns ---

	@Test
	public void questionMarkWildcard() {
		PathPatternMatcher matcher = createMatcher("?-test.xml");
		assertTrue(matcher.matches("file:///home/user/project/a-test.xml"));
		assertFalse(matcher.matches("file:///home/user/project/ab-test.xml"));
	}

	@Test
	public void questionMarkMatchesInSubdirectory() {
		PathPatternMatcher matcher = createMatcher("?.xml");
		assertTrue(matcher.matches("file:///home/user/deep/path/a.xml"));
		assertFalse(matcher.matches("file:///home/user/ab.xml"));
	}

	@Test
	public void multipleQuestionMarks() {
		PathPatternMatcher matcher = createMatcher("???.xml");
		assertTrue(matcher.matches("file:///home/user/abc.xml"));
		assertFalse(matcher.matches("file:///home/user/ab.xml"));
		assertFalse(matcher.matches("file:///home/user/abcd.xml"));
	}

	@Test
	public void questionMarkAndWildcard() {
		PathPatternMatcher matcher = createMatcher("?-*.xml");
		assertTrue(matcher.matches("file:///home/user/a-test.xml"));
		assertTrue(matcher.matches("file:///home/user/b-foo.xml"));
		assertFalse(matcher.matches("file:///home/user/ab-test.xml"));
	}

	// --- Plain filename patterns ---

	@Test
	public void plainFileName() {
		PathPatternMatcher matcher = createMatcher("web.xml");
		assertTrue(matcher.matches("file:///home/user/project/web.xml"));
		assertTrue(matcher.matches("file:///a/b/c/web.xml"));
		assertFalse(matcher.matches("file:///home/user/project/other.xml"));
	}

	@Test
	public void patternStartingWithName() {
		PathPatternMatcher matcher = createMatcher("myFile*.xml");
		assertTrue(matcher.matches("file:///home/user/project/myFile1.xml"));
		assertTrue(matcher.matches("file:///test/myFile.xml"));
		assertFalse(matcher.matches("file:///test/otherFile.xml"));
	}

	@Test
	public void plainFileNameWithMultipleDots() {
		PathPatternMatcher matcher = createMatcher("project.build.xml");
		assertTrue(matcher.matches("file:///home/user/project.build.xml"));
		assertFalse(matcher.matches("file:///home/user/project.xml"));
	}

	// --- matches(URI) overload ---

	@Test
	public void matchesWithURIObjectSingleWildcard() throws Exception {
		PathPatternMatcher matcher = createMatcher("*-suffix.xml");
		assertTrue(matcher.matches(new URI("file:///home/user/project/file-suffix.xml")));
		assertFalse(matcher.matches(new URI("file:///home/user/project/file.xml")));
	}

	@Test
	public void matchesWithURIObjectDoubleWildcard() throws Exception {
		PathPatternMatcher matcher = createMatcher("**/*-suffix.xml");
		assertTrue(matcher.matches(new URI("file:///home/user/project/file-suffix.xml")));
		assertFalse(matcher.matches(new URI("file:///home/user/project/file.xml")));
	}

	// --- setPattern resets pathMatcher ---

	@Test
	public void setPatternResetsPathMatcher() {
		PathPatternMatcher matcher = createMatcher("*.xml");
		assertTrue(matcher.matches("file:///test/file.xml"));
		assertFalse(matcher.matches("file:///test/file.json"));

		matcher.setPattern("*.json");
		assertTrue(matcher.matches("file:///test/file.json"));
		assertFalse(matcher.matches("file:///test/file.xml"));
	}

	// --- Edge cases ---

	@Test
	public void emptyPatternDoesNotMatch() {
		PathPatternMatcher matcher = createMatcher("");
		assertFalse(matcher.matches("file:///test/file.xml"));
	}

	@Test
	public void invalidURIDoesNotMatch() {
		PathPatternMatcher matcher = createMatcher("*.xml");
		assertFalse(matcher.matches("not a valid uri"));
	}

	@Test
	public void patternWithBrackets() {
		PathPatternMatcher matcher = createMatcher("[abc].xml");
		assertTrue(matcher.matches("file:///home/user/a.xml"));
		assertTrue(matcher.matches("file:///home/user/b.xml"));
		assertTrue(matcher.matches("file:///home/user/c.xml"));
		assertFalse(matcher.matches("file:///home/user/d.xml"));
	}

	@Test
	public void patternWithBraces() {
		PathPatternMatcher matcher = createMatcher("*.{xml,xsd}");
		assertTrue(matcher.matches("file:///home/user/test.xml"));
		assertTrue(matcher.matches("file:///home/user/test.xsd"));
		assertFalse(matcher.matches("file:///home/user/test.json"));
	}

	@Test
	public void singleCharacterPattern() {
		PathPatternMatcher matcher = createMatcher("a");
		assertTrue(matcher.matches("file:///home/user/a"));
		assertFalse(matcher.matches("file:///home/user/b"));
	}

	// --- Invalid glob patterns (must not throw) ---

	@Test
	public void unclosedBracketDoesNotThrow() {
		PathPatternMatcher matcher = createMatcher("[unclosed");
		assertFalse(matcher.matches("file:///home/user/test.xml"));
	}

	@Test
	public void unclosedBraceDoesNotThrow() {
		PathPatternMatcher matcher = createMatcher("{unclosed");
		assertFalse(matcher.matches("file:///home/user/test.xml"));
	}

	@Test
	public void unclosedBracketViaURIDoesNotThrow() throws Exception {
		PathPatternMatcher matcher = createMatcher("[invalid");
		assertFalse(matcher.matches(new URI("file:///home/user/test.xml")));
	}

	@Test
	public void unclosedBraceViaURIDoesNotThrow() throws Exception {
		PathPatternMatcher matcher = createMatcher("{invalid");
		assertFalse(matcher.matches(new URI("file:///home/user/test.xml")));
	}

	// --- Directory-prefixed patterns ---

	@Test
	public void patternWithDirectoryPrefix() {
		PathPatternMatcher matcher = createMatcher("src/*.xml");
		assertTrue(matcher.matches("file:///home/user/project/src/test.xml"));
		assertFalse(matcher.matches("file:///home/user/project/other/test.xml"));
	}

	// --- Absolute path patterns ---

	@Test
	public void absoluteUnixPathPattern() {
		PathPatternMatcher matcher = createMatcher("/home/user/project/*.xml");
		assertTrue(matcher.matches("file:///home/user/project/test.xml"));
		assertFalse(matcher.matches("file:///other/path/test.xml"));
	}

	@Test
	public void absoluteUnixPathPatternExactFile() {
		PathPatternMatcher matcher = createMatcher("/home/user/project/test.xml");
		assertTrue(matcher.matches("file:///home/user/project/test.xml"));
		assertFalse(matcher.matches("file:///home/user/project/other.xml"));
	}

	@Test
	public void absoluteUnixPathPatternWithDoubleWildcard() {
		PathPatternMatcher matcher = createMatcher("/home/user/**/*.xml");
		assertTrue(matcher.matches("file:///home/user/project/test.xml"));
		assertTrue(matcher.matches("file:///home/user/a/b/c/test.xml"));
		assertFalse(matcher.matches("file:///other/user/test.xml"));
	}

	// --- Null / missing pattern ---

	@Test
	public void nullPatternDoesNotMatch() {
		PathPatternMatcher matcher = new PathPatternMatcher();
		assertFalse(matcher.matches("file:///test/file.xml"));
	}

	@Test
	public void nullPatternDoesNotMatchURI() throws Exception {
		PathPatternMatcher matcher = new PathPatternMatcher();
		assertFalse(matcher.matches(new java.net.URI("file:///test/file.xml")));
	}

	private static PathPatternMatcher createMatcher(String pattern) {
		PathPatternMatcher matcher = new PathPatternMatcher();
		matcher.setPattern(pattern);
		return matcher;
	}
}
