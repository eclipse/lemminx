/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.MockXMLLanguageServer;
import org.eclipse.lemminx.dom.DOMDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IDocumentLifecycleParticipant}
 */
public class DocumentLifecycleParticipantTest extends AbstractCacheBasedTest {

	private static class CaptureDocumentLifecycleCalls implements IDocumentLifecycleParticipant {

		private int didOpen;
		private int didChange;
		private int didSave;
		private int didClose;

		@Override
		public void didOpen(DOMDocument document) {
			if (document != null) {
				this.didOpen++;
			}
		}

		@Override
		public void didChange(DOMDocument document) {
			if (document != null) {
				this.didChange++;
			}
		}

		@Override
		public void didSave(DOMDocument document) {
			if (document != null) {
				this.didSave++;
			}
		}

		@Override
		public void didClose(DOMDocument document) {
			if (document != null) {
				this.didClose++;
			}
		}

		public int getDidOpen() {
			return didOpen;
		}

		public int getDidChange() {
			return didChange;
		}

		public int getDidSave() {
			return didSave;
		}

		public int getDidClose() {
			return didClose;
		}

	}

	private CaptureDocumentLifecycleCalls documentLifecycleParticipant;
	private MockXMLLanguageServer server;

	@BeforeEach
	public void initializeLanguageService() {
		this.server = new MockXMLLanguageServer();
		this.documentLifecycleParticipant = new CaptureDocumentLifecycleCalls();
		server.getXMLLanguageService().registerDocumentLifecycleParticipant(this.documentLifecycleParticipant);
	}

	@Test
	public void didOpen() {
		assertEquals(0, documentLifecycleParticipant.getDidOpen());
		server.didOpen("test.xml", "<foo ");
		waitFor();
		assertEquals(1, documentLifecycleParticipant.getDidOpen());
	}

	@Test
	public void didChange() {
		assertEquals(0, documentLifecycleParticipant.getDidChange());
		server.didOpen("test.xml", "<foo ");
		server.didChange("test.xml", Collections.emptyList());
		waitFor();
		assertEquals(1, documentLifecycleParticipant.getDidChange());
	}

	@Test
	public void didSave() {
		assertEquals(0, documentLifecycleParticipant.getDidSave());
		server.didOpen("test.xml", "<foo ");
		server.didSave("test.xml");
		waitFor();
		assertEquals(1, documentLifecycleParticipant.getDidSave());
	}

	@Test
	public void didClose() {
		assertEquals(0, documentLifecycleParticipant.getDidClose());
		server.didOpen("test.xml", "<foo ");
		server.didClose("test.xml");
		assertEquals(1, documentLifecycleParticipant.getDidClose());
	}

	private static void waitFor() {
		try {
			Thread.sleep(600);
		} catch (Exception e) {
		}
	}
}
