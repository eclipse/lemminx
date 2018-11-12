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

import org.junit.Test;

public class CacheResourcesManagerTest {

	@Test
	public void testCanUseCache() {
		testCanUseCache(true);
		testCanUseCache(false);
	}

	private void testCanUseCache(boolean useCacheEnabled) {
		CacheResourcesManager cacheResourcesManager = new CacheResourcesManager();
		cacheResourcesManager.setUseCache(useCacheEnabled);
		assertEquals(useCacheEnabled, cacheResourcesManager.canUseCache("http://foo"));
		assertEquals(useCacheEnabled, cacheResourcesManager.canUseCache("ftp://foo"));
		assertEquals(useCacheEnabled, cacheResourcesManager.canUseCache("https://foo"));
		assertFalse(cacheResourcesManager.canUseCache("file:///foo"));
	}

}