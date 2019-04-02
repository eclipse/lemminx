/*******************************************************************************
* Copyright (c) 2018 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.uriresolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.eclipse.lsp4xml.AbstractCacheBasedTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CacheResourcesManagerTest extends AbstractCacheBasedTest {

	private CacheResourcesManager cacheResourcesManager;

	private FileServer server;

	
	@Before
	public void setup() throws Exception {
		cacheResourcesManager = new CacheResourcesManager(testingCache());
		cacheResourcesManager.setUseCache(true);
	}

	@After
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
		} catch (CacheResourceDownloadingException ignored) {
		}
		TimeUnit.MILLISECONDS.sleep(200);
		//failed to download so returns null
		assertNull(cacheResourcesManager.getResource(uri));

		TimeUnit.SECONDS.sleep(1);//wait past the cache expiration date

		//Manager should retry downloading
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
		try {
			cacheResourcesManager.getResource(uri);
			fail("cacheResourcesManager should be busy downloading the url");
		} catch (CacheResourceDownloadingException ignored) {
		}
		TimeUnit.MILLISECONDS.sleep(200);
		assertNotNull(cacheResourcesManager.getResource(uri));

		server.stop();
		TimeUnit.SECONDS.sleep(1);//wait past the cache expiration date

		//Manager should return cached content, even if server is offline
		assertNotNull(cacheResourcesManager.getResource(uri));
	}

	private Cache<String, Boolean> testingCache() {
		return CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).maximumSize(1).build();
	}
}