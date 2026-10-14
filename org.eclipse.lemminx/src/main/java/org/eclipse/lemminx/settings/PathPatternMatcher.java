/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathPatternMatcher {

	private static final Logger LOGGER = Logger.getLogger(PathPatternMatcher.class.getName());

	private transient PathMatcher pathMatcher;
	private String pattern;

	public String getPattern() {
		return pattern;
	}

	public PathPatternMatcher setPattern(String pattern) {
		this.pattern = pattern;
		this.pathMatcher = null;
		return this;
	}

	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	public boolean matches(String uri) {
		try {
			return matches(new URI(uri));
		} catch (Exception e) {
			return false;
		}
	}

	public boolean matches(URI uri) {
		if (pattern == null || pattern.isEmpty()) {
			return false;
		}
		if (pathMatcher == null) {
			try {
				pathMatcher = createGlobMatcher(pattern);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Invalid glob pattern: " + pattern, e);
				return false;
			}
		}
		try {
			return pathMatcher.matches(Paths.get(uri));
		} catch (Exception e) {
			return false;
		}
	}

	protected static PathMatcher createGlobMatcher(String pattern) {
		String glob = pattern;
		if (!isAbsolutePattern(glob) && !isGlobStarPattern(glob)) {
			glob = "**/" + glob;
		}
		return FileSystems.getDefault().getPathMatcher("glob:" + glob);
	}

	private static boolean isAbsolutePattern(String pattern) {
		if (pattern.charAt(0) == '/') {
			return true;
		}
		return pattern.length() >= 2
				&& Character.isLetter(pattern.charAt(0))
				&& pattern.charAt(1) == ':';
	}

	private static boolean isGlobStarPattern(String pattern) {
		return pattern.charAt(0) == '*'
				&& pattern.length() >= 2
				&& pattern.charAt(1) == '*';
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}

		if(obj instanceof PathPatternMatcher) {
			PathPatternMatcher other = (PathPatternMatcher) obj;
			if(!Objects.equals(pathMatcher, other.getPathMatcher())) {
				return false;
			}
			if(!Objects.equals(pattern, other.getPattern())) {
				return false;
			}
			return true;
		}
		return false;
	}

}