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

import org.eclipse.lemminx.client.LimitExceededWarner;
import org.eclipse.lemminx.client.LimitFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code LimitExceededWarner}
 */
public class LimitExceededWarnerTest extends AbstractNotifierTest {

	private final LimitFeature TEST_FEATURE = LimitFeature.SYMBOLS;
	private static final int TEST_LIMIT = 0;
	private LimitExceededWarner limitExceededWarner;

	@BeforeEach
	public void before() {
		super.before();
		this.limitExceededWarner = getTextDocumentService().getLimitExceededWarner();
	}

	@Test
	public void testSendActionableNotification() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(true, true);

		sendNotification(uri1);
		assertCounts(1, 0);

		sendNotification(uri1);
		assertCounts(1, 0);

		sendNotification(uri2);
		assertCounts(2, 0);

		sendNotification(uri2);
		assertCounts(2, 0);
	}

	@Test
	public void testActionableNotificationEvict() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(true, true);

		sendNotification(uri1);
		assertCounts(1, 0);
		sendNotification(uri2);
		assertCounts(2, 0);

		getTextDocumentService().getLimitExceededWarner().evictValue(uri1);

		sendNotification(uri1);
		assertCounts(3, 0);
		sendNotification(uri2);
		assertCounts(3, 0);

		getTextDocumentService().getLimitExceededWarner().evictValue(uri2);

		sendNotification(uri1);
		assertCounts(3, 0);
		sendNotification(uri2);
		assertCounts(4, 0);
	}

	@Test
	public void testSendMessage() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(false, false);

		sendNotification(uri1);
		assertCounts(0, 1);

		sendNotification(uri1);
		assertCounts(0, 1);

		sendNotification(uri2);
		assertCounts(0, 2);

		sendNotification(uri2);
		assertCounts(0, 2);
	}

	@Test
	public void testSendMessage2() {
		String uri = "file:///uri.xml";
		setSupportCapabilities(true, false);
		sendNotification(uri);
		assertCounts(0, 1);
	}

	@Test
	public void testSendMessage3() {
		String uri = "file:///uri.xml";
		setSupportCapabilities(false, true);
		sendNotification(uri);
		assertCounts(0, 1);
	}

	@Test
	public void testSendMessageEvict() {
		String uri1 = "file:///uri1.xml";
		String uri2 = "file:///uri2.xml";
		setSupportCapabilities(false, false);

		sendNotification(uri1);
		assertCounts(0, 1);
		sendNotification(uri2);
		assertCounts(0, 2);

		getTextDocumentService().getLimitExceededWarner().evictValue(uri1);

		sendNotification(uri1);
		assertCounts(0, 3);
		sendNotification(uri2);
		assertCounts(0, 3);

		getTextDocumentService().getLimitExceededWarner().evictValue(uri2);

		sendNotification(uri1);
		assertCounts(0, 3);
		sendNotification(uri2);
		assertCounts(0, 4);
	}

	private void sendNotification(String uri) {
		this.limitExceededWarner
				.onResultLimitExceeded(uri, TEST_LIMIT, TEST_FEATURE);
	}
}