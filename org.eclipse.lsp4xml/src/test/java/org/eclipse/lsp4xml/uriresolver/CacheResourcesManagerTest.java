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

import org.junit.After;
import org.junit.Test;

public class CacheResourcesManagerTest {

	@After
	public void tearDown() {
		CacheResourcesManager.getInstance().setUseCache(false);
	}

	@Test
	public void testCanUseCache() {
		testCanUseCache(true);
		testCanUseCache(false);
	}

	public void testCanUseCache(boolean useCacheEnabled) {
		CacheResourcesManager.getInstance().setUseCache(useCacheEnabled);
		assertEquals(useCacheEnabled, CacheResourcesManager.getInstance().canUseCache("http://foo"));
		assertEquals(useCacheEnabled, CacheResourcesManager.getInstance().canUseCache("ftp://foo"));
		assertEquals(useCacheEnabled, CacheResourcesManager.getInstance().canUseCache("https://foo"));
		assertFalse(CacheResourcesManager.getInstance().canUseCache("file:///foo"));
	}

}