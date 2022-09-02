/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
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
package org.eclipse.lemminx.extensions.contentmodel.settings;

import org.eclipse.lemminx.settings.PathPatternMatcher;

/**
 * XML validation settings used for a given pattern file.
 * 
 * <code>
 * {
      "pattern": "**{.project,.classpath,plugin.xml,feature.xml,category.xml,.target,.product}",
      "noGrammar": "ignore"
   }
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class XMLValidationFilter extends XMLValidationSettings {

	private String pattern;

	private transient PathPatternMatcher matcher;

	public boolean matches(String uri) {
		if (matcher == null) {
			matcher = new PathPatternMatcher();
			matcher.setPattern(pattern);
		}
		return matcher.matches(uri);
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		this.matcher = null;
	}
}
