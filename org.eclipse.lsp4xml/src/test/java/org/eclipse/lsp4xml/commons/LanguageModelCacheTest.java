/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.commons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.eclipse.lsp4xml.utils.IOUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test with language model cache
 * 
 * @author Angelo ZERR
 *
 */
public class LanguageModelCacheTest {

	@Test
	public void testModelCache() {

		LanguageModelCache<DOMDocument> cache = new LanguageModelCache<DOMDocument>(10, 60, new TextDocuments(),
				(document, monitor) -> {
					return DOMParser.getInstance().parse(document, null);
				});

		Set<DOMDocument> documents = new HashSet<DOMDocument>();

		String text = IOUtils.convertStreamToString(LanguageModelCacheTest.class.getResourceAsStream("/xml/nasa.xml"));
		int version = 0;

		AtomicBoolean hasCancellationException = new AtomicBoolean(false);

		// Create 100 threads which get the nasa.xml file from the cache
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < 100; i++) {

			final TextDocumentItem document = new TextDocumentItem();
			document.setLanguageId("xml");
			document.setUri("test.xml");
			document.setText(text);

			if (i % 20 == 0) {
				document.setVersion(version++);
			}
			Thread t = new Thread(() -> {
				try {
					// get DOM document from the cache and parse it if needed
					DOMDocument dom = cache.get(document);
					documents.add(dom);
				} catch (CancellationException e) {
					// This exception occurs when document version is older than cache model version
					hasCancellationException.set(true);
				}
			});
			threads.add(t);
		}

		// Execute the threads
		for (Thread thread : threads) {
			thread.start();
		}

		// Wait for all processes of the threads
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		Assert.assertTrue("Has older version than model cache version", hasCancellationException.get());
		Assert.assertTrue("DOM document instances", documents.size() <= version);

	}
}
