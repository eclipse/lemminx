/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.lemminx.client.InvalidPathWarner;
import org.eclipse.lemminx.client.PathFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code InvalidPathWarner}
 */
public class InvalidPathWarnerTest extends AbstractNotifierTest {

	private static final String catalog1 = "catalog.xml";

	private static final String catalog2 = "catalog2.xml";

	private static final PathFeature TEST_FEATURE = PathFeature.CATALOGS;
	
	private InvalidPathWarner pathWarner;

	@BeforeEach
	public void before() {
		super.before();
		this.pathWarner = new InvalidPathWarner(this.languageServer);
	}

	@Test
	public void testSendActionableNotification() {
		setSupportCapabilities(true, true);
		sendNotification(catalog1);
		assertCounts(1, 0);
	}

	@Test
	public void testSendActionableNotificationWarningText() {
		setSupportCapabilities(true, true);
		sendNotification(catalog1);
		assertMessage("Invalid path for setting 'xml.catalogs': '" + catalog1 + "'");
		sendNotification(catalog1, catalog2);
		assertMessage("Invalid paths for setting 'xml.catalogs': '" + catalog1 + "', '" + catalog2 + "'");
	}

	@Test
	public void testSendActionableNotificationCache() {

		setSupportCapabilities(true, true);

		// adds catalog1 to cache
		sendNotification(catalog1);
		assertCounts(1, 0);

		sendNotification(catalog1);
		assertCounts(1, 0);

		// clears cache for catalog1, adds catalog2 to cache
		sendNotification(catalog2);
		assertCounts(2, 0);

		sendNotification(catalog2);
		assertCounts(2, 0);

		// clears cache for catalog2, adds catalog1 to cache
		sendNotification(catalog1);
		assertCounts(3, 0);

		sendNotification(catalog1);
		assertCounts(3, 0);
	}

	@Test
	public void testActionableNotificationEvict() {
		setSupportCapabilities(true, true);

		sendNotification(catalog1);
		assertCounts(1, 0);

		this.pathWarner.evictValue(catalog1);

		sendNotification(catalog1);
		assertCounts(2, 0);

		sendNotification(catalog1);
		assertCounts(2, 0);
	}

	@Test
	public void testSendMessage() {
		setSupportCapabilities(false, false);
		sendNotification(catalog1);
		assertCounts(0, 1);
	}

	@Test
	public void testSendMessage2() {
		setSupportCapabilities(true, false);
		sendNotification(catalog1);
		assertCounts(0, 1);
	}

	@Test
	public void testSendMessage3() {
		setSupportCapabilities(false, true);
		sendNotification(catalog1);
		assertCounts(0, 1);
	}

	@Test
	public void testSendMessageWarningText() {
		setSupportCapabilities(false, false);
		sendNotification(catalog1);
		assertMessage("Invalid path for setting 'xml.catalogs': '" + catalog1 + "'");
		sendNotification(catalog1, catalog2);
		assertMessage("Invalid paths for setting 'xml.catalogs': '" + catalog1 + "', '" + catalog2 + "'");
	}

	@Test
	public void testSendMessageCache() {
		setSupportCapabilities(false, false);

		// adds catalog1 to cache
		sendNotification(catalog1);
		assertCounts(0, 1);

		sendNotification(catalog1);
		assertCounts(0, 1);

		// clears cache for catalog1, adds catalog2 to cache
		sendNotification(catalog2);
		assertCounts(0, 2);

		sendNotification(catalog2);
		assertCounts(0, 2);

		// clears cache for catalog2, adds catalog1 to cache
		sendNotification(catalog1);
		assertCounts(0, 3);

		sendNotification(catalog1);
		assertCounts(0, 3);
	}

	@Test
	public void testSendMessageEvict() {
		setSupportCapabilities(false, false);

		sendNotification(catalog1);
		assertCounts(0, 1);

		this.pathWarner.evictValue(catalog1);

		sendNotification(catalog1);
		assertCounts(0, 2);

		sendNotification(catalog1);
		assertCounts(0, 2);
	}

	private void sendNotification(String... invalidPaths) {
		this.pathWarner
				.onInvalidFilePath(new LinkedHashSet<String>(Arrays.asList(invalidPaths)), TEST_FEATURE);
	}
}