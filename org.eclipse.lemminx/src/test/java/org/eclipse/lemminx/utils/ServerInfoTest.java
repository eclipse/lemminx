/**
 *  Copyright (c) 2018-2020 Red Hat, Inc.
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
package org.eclipse.lemminx.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerInfoTest {
	private static final String JAVA_HOME = "/foo/bar/java/";

	private ServerInfo serverInfo;

	@BeforeEach
	public void setup() {
		Properties sysProps = new Properties();
		sysProps.setProperty("java.home", JAVA_HOME);
		serverInfo = new ServerInfo(sysProps);
	}

	@Test
	public void testVersion() {
		String version = serverInfo.getVersion();
		Pattern pattern = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)(-.*)$");
		Matcher matcher = pattern.matcher(version);
		assertTrue(matcher.matches(), "Unexpected format for :" + version);
	}

	@Test
	public void testJavaHome() {
		assertEquals( JAVA_HOME, serverInfo.getJava(),"Unexpected Java home");
	}

	@Test
	public void testGitInfos() {
		assertNotNull(serverInfo.getBranch(), "Branch was not set");
		assertNotNull(serverInfo.getCommitMessage(), "Commit message was not set");
		assertNotNull(serverInfo.getShortCommitId(), "Commit id was not set");
	}

	@Test
	public void testDetails() {
		String details = serverInfo.details();
		//Check we didn't miss any info:
		assertTrue(details.contains(serverInfo.getVersion()), "version is missing from the details");
		assertTrue(details.contains(serverInfo.getJava()), "Java is missing from the details");
		assertTrue(details.contains(serverInfo.getCommitMessage()), "commit message is missing from the details");
		assertTrue(details.contains(serverInfo.getShortCommitId()), "commit id is missing from the details");
		
		String branch = serverInfo.getBranch();
		if (ServerInfo.MASTER.equals(branch)) {
			assertFalse(details.contains(branch), "master branch should not be in the details");
		} else {
			assertTrue(details.contains(branch), branch + " branch is missing from the details");
		}
	}
}