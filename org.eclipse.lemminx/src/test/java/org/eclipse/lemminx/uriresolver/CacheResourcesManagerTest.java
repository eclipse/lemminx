/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.uriresolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CacheResourcesManagerTest extends AbstractCacheBasedTest {

	private CacheResourcesManager cacheResourcesManager;

	private FileServer server;

	@BeforeEach
	public void setup() throws Exception {
		cacheResourcesManager = new CacheResourcesManager(testingCache());
		cacheResourcesManager.setUseCache(true);
	}

	@AfterEach
	public void stopServer() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	@Test
	public void testCanUseCache() {
		testCanUseCache(true);
		testCanUseCache(false);
	}

	private void testCanUseCache(boolean useCacheEnabled) {
		cacheResourcesManager.setUseCache(useCacheEnabled);
		assertEquals(useCacheEnabled, cacheResourcesManager.canUseCache("http://foo"));
		assertEquals(useCacheEnabled, cacheResourcesManager.canUseCache("ftp://foo"));
		assertEquals(useCacheEnabled, cacheResourcesManager.canUseCache("https://foo"));
		assertFalse(cacheResourcesManager.canUseCache("file:///foo"));
	}

	@Test
	public void testUnavailableCache() throws Exception {
		FileServer server = new FileServer();
		server.start();
		String uri = server.getUri("bad/url");
		try {
			cacheResourcesManager.getResource(uri);
			fail("cacheResourcesManager should be busy downloading the url");
		} catch (CacheResourceDownloadingException containsFuture) {
			try {
				containsFuture.getFuture().get(30, TimeUnit.SECONDS);
				fail("Download should have failed");
			} catch (ExecutionException failedDownload) {
				// Failed to download file, so exception is thrown
				if (!(failedDownload.getCause() instanceof CacheResourceDownloadedException)) {
					fail("Incorrect exception thrown during failed download");
				}
			}
			assertNull(cacheResourcesManager.getResource(uri));
		}
		TimeUnit.SECONDS.sleep(2); // wait past the cache expiration date
		try {
			cacheResourcesManager.getResource(uri);
			fail("cacheResourcesManager should be busy re-downloading the url");
		} catch (CacheResourceDownloadingException ignored) {
		}
	}

	@Test
	public void testAvailableCache() throws Exception {
		FileServer server = new FileServer();
		server.start();
		String uri = server.getUri("/dtd/web-app_2_3.dtd");
		Path path = null;
		try {
			cacheResourcesManager.getResource(uri);
			fail("cacheResourcesManager should be busy downloading the url");
		} catch (CacheResourceDownloadingException containsFuture) {
			path = containsFuture.getFuture().get(30, TimeUnit.SECONDS);
		}
		assertNotNull(path);
		assertNotNull(cacheResourcesManager.getResource(uri));
		server.stop();
		TimeUnit.SECONDS.sleep(2); // wait past the cache expiration date
		cacheResourcesManager.getResource(uri);
		// Manager should return cached content, even if server is offline
		assertNotNull(cacheResourcesManager.getResource(uri));
	}

	@Test
	public void testGetBadResource() throws IOException {
		CacheResourceDownloadingException actual = null;
		try {
			cacheResourcesManager.getResource("http://localhost/../../../../../test.txt");
		} catch (CacheResourceDownloadingException e) {
			actual = e;
		}
		assertNotNull(actual);
		assertEquals(
				"The resource 'http://localhost/../../../../../test.txt' cannot be downloaded in the cache path.",
				actual.getMessage());
	}

	private Cache<String, Boolean> testingCache() {
		return CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).maximumSize(1).build();
	}
}