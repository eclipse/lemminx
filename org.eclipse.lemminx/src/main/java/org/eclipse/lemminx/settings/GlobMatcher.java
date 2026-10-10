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

import java.util.List;

/**
 * Simple textual glob matching utility for non-file-path strings such as
 * namespace URIs, DOCTYPE public/system IDs, and grammar URIs.
 *
 * <p>
 * Java NIO {@link java.nio.file.PathMatcher} only works for file-system
 * paths. This class provides lightweight glob matching for arbitrary
 * text strings using two wildcards:
 * </p>
 * <ul>
 * <li>{@code *} — matches any sequence of characters (including empty)</li>
 * <li>{@code ?} — matches exactly one character</li>
 * </ul>
 *
 * <h3>Use case: match DocBook namespace with trailing wildcard</h3>
 * <pre>
 * GlobMatcher.match("http://docbook.org/ns/docbook*", "http://docbook.org/ns/docbook");     // true
 * GlobMatcher.match("http://docbook.org/ns/docbook*", "http://docbook.org/ns/docbook/5.1"); // true
 * </pre>
 *
 * <h3>Use case: match any MyBatis DTD version</h3>
 * <pre>
 * GlobMatcher.match("-//mybatis.org//DTD Mapper*", "-//mybatis.org//DTD Mapper 3.0//EN"); // true
 * </pre>
 *
 * @see DocumentMatcher
 */
public final class GlobMatcher {

	private GlobMatcher() {
	}

	/**
	 * Matches a glob pattern against a text string.
	 *
	 * <p>
	 * Supports path-glob {@code **}{@code /} sequences: when {@code **} appears
	 * as a complete path component (at the start of the pattern or after
	 * {@code /}), it matches zero or more path segments. Any non-empty prefix
	 * consumed by {@code **} must end at a {@code /} boundary.
	 * </p>
	 *
	 * <p>Use case:</p>
	 * <ul>
	 * <li>{@code **}{@code /my-schema.dtd} matches {@code my-schema.dtd}
	 * and {@code path/to/my-schema.dtd}</li>
	 * <li>{@code **}{@code /my-schema.dtd} does <b>not</b> match
	 * {@code other-my-schema.dtd}</li>
	 * </ul>
	 *
	 * @param pattern the glob pattern (supports {@code *}, {@code ?},
	 *                and {@code **}{@code /}).
	 * @param text    the text to match.
	 * @return {@code true} if the text matches the pattern.
	 */
	public static boolean match(String pattern, String text) {
		int dsi = indexOfDoubleStarSlash(pattern);
		if (dsi >= 0) {
			// Zero directories: remove **/ and try matching the rest
			String zeroDir = pattern.substring(0, dsi) + pattern.substring(dsi + 3);
			if (match(zeroDir, text)) {
				return true;
			}
		}
		return matchCore(pattern, text);
	}

	/**
	 * Path-aware glob matching. Behaves like {@link #match(String, String)}
	 * but also tries matching against the filename portion of the text
	 * (after the last {@code /}) when the pattern contains no {@code /}.
	 *
	 * <p>
	 * Use this for values that are typically paths or URIs with path
	 * structure — such as DOCTYPE system IDs, grammar URIs, and
	 * xml-model hrefs — where a bare filename pattern like
	 * {@code my-schema.dtd} should match {@code path/to/my-schema.dtd}.
	 * </p>
	 *
	 * <p>
	 * Do <b>not</b> use this for namespace URIs or public IDs, where
	 * a pattern like {@code docbook} must not accidentally match
	 * {@code http://example.com/ns/docbook}.
	 * </p>
	 *
	 * @param pattern the glob pattern.
	 * @param text    the text to match.
	 * @return {@code true} if the text matches the pattern.
	 */
	public static boolean matchPath(String pattern, String text) {
		if (pattern.indexOf('/') < 0) {
			int lastSlash = text.lastIndexOf('/');
			if (lastSlash >= 0 && match(pattern, text.substring(lastSlash + 1))) {
				return true;
			}
		}
		return match(pattern, text);
	}

	/**
	 * Finds the position of the first {@code **}{@code /} that acts as a
	 * complete path component (at the start or after {@code /}).
	 *
	 * @return the index of the first {@code *} in the sequence, or -1.
	 */
	private static int indexOfDoubleStarSlash(String pattern) {
		if (pattern.startsWith("**/")) {
			return 0;
		}
		int idx = pattern.indexOf("/**/");
		return idx >= 0 ? idx + 1 : -1;
	}

	/**
	 * Core iterative glob matching. Uses a greedy algorithm with a single
	 * backtrack point, running in O(n·m) time. Consecutive {@code *}
	 * wildcards are collapsed to a single {@code *}.
	 */
	private static boolean matchCore(String pattern, String text) {
		int pi = 0, ti = 0;
		int starPi = -1, starTi = -1;

		while (ti < text.length()) {
			if (pi < pattern.length() && pattern.charAt(pi) == '*') {
				// Collapse consecutive stars (e.g. "**" behaves like "*")
				while (pi < pattern.length() && pattern.charAt(pi) == '*') {
					pi++;
				}
				// Record position after star(s) for backtracking
				starPi = pi;
				starTi = ti;
			} else if (pi < pattern.length()
					&& (pattern.charAt(pi) == '?' || pattern.charAt(pi) == text.charAt(ti))) {
				pi++;
				ti++;
			} else if (starPi >= 0) {
				// Mismatch after a star: backtrack — let the star consume
				// one more character from text and retry from there.
				pi = starPi;
				ti = ++starTi;
			} else {
				return false;
			}
		}
		// Trailing stars match empty suffix
		while (pi < pattern.length() && pattern.charAt(pi) == '*') {
			pi++;
		}
		return pi == pattern.length();
	}

	/**
	 * Checks if any glob pattern in the list matches the given value
	 * using full-text matching ({@link #match(String, String)}).
	 *
	 * <p>
	 * Use case: check if a document's namespace URI or public ID matches
	 * any of the configured patterns (OR semantics).
	 * </p>
	 *
	 * @param patterns the list of glob patterns.
	 * @param value    the value to match, or {@code null}.
	 * @return {@code true} if any pattern matches the value; {@code false}
	 *         if the value is {@code null} or no pattern matches.
	 */
	public static boolean matchAny(List<String> patterns, String value) {
		if (value == null) {
			return false;
		}
		for (String p : patterns) {
			if (p != null && match(p, value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if any glob pattern in the list matches the given value
	 * using path-aware matching ({@link #matchPath(String, String)}).
	 *
	 * <p>
	 * Use case: check if a document's system ID or grammar URI matches
	 * any of the configured patterns (OR semantics), allowing bare
	 * filename patterns to match path-like values.
	 * </p>
	 *
	 * @param patterns the list of glob patterns.
	 * @param value    the value to match, or {@code null}.
	 * @return {@code true} if any pattern matches the value; {@code false}
	 *         if the value is {@code null} or no pattern matches.
	 */
	public static boolean matchPathAny(List<String> patterns, String value) {
		if (value == null) {
			return false;
		}
		for (String p : patterns) {
			if (p != null && matchPath(p, value)) {
				return true;
			}
		}
		return false;
	}
}
