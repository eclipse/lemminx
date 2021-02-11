/**
 *  Copyright (c) 2018-2021 Red Hat, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Fred Bricon <fbricon@gmail.com>, Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.utils.platform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class PlatformTest {

	@Test
	public void testVersion() {
		String version = Platform.getVersion().getVersionNumber();
		Pattern pattern = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)(-.*)$");
		Matcher matcher = pattern.matcher(version);
		assertTrue(matcher.matches(), "Unexpected format for :" + version);
	}

	@Test
	public void testGitInfos() {
		assertNotNull(Platform.getVersion().getBranch(), "Branch was not set");
		assertNotNull(Platform.getVersion().getCommitMessage(), "Commit message was not set");
		assertNotNull(Platform.getVersion().getShortCommitId(), "Commit id was not set");
	}

	@Test
	public void testDetails() {
		String details = Platform.details();
		// Check we didn't miss any info:
		assertTrue(details.contains(Platform.getVersion().getVersionNumber()), "version is missing from the details");
		assertTrue(details.contains(Platform.getJVM().getJavaHome()), "Java is missing from the details");
		assertTrue(details.contains(Platform.getVersion().getCommitMessage()), "commit message is missing from the details");
		assertTrue(details.contains(Platform.getVersion().getShortCommitId()), "commit id is missing from the details");

		String branch = Platform.getVersion().getBranch();
		if (Version.MAIN_BRANCH.equals(branch)) {
			assertFalse(details.contains(branch), "master branch should not be in the details");
		} else {
			assertTrue(details.contains(branch), branch + " branch is missing from the details");
		}
	}
}