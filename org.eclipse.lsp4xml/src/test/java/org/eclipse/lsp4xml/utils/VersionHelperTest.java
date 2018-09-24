/**
 *  Copyright (c) 2018 Red Hat, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Fred Bricon <fbricon@gmail.com>, Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.utils;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class VersionHelperTest {

	@Test
	public void testGetVersion() {
		String version = VersionHelper.getVersion();
		Pattern pattern = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)(-.*)$");
		Matcher matcher = pattern.matcher(version);
		assertTrue("Unexpected format for :" + version, matcher.matches());
	}
}