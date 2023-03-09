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
package org.eclipse.lemminx.telemetry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Store telemetry data for transmission at a later time
 */
public class TelemetryCache {

	private Map<String, Object> cache = new HashMap<>();

	public void put(String key) {
		cache.put(key, ((Integer)cache.getOrDefault(key, 0)) + 1);
	}

	public void put(String key, String value) {
		Set<String> tmp = new HashSet<>();
		Object val = cache.get(key);
		if (val == null) {
			tmp.add(value);
			cache.put(key, tmp);
		} else {
			((Set<String>) cache.get(key)).add(value);
		}
	}

	public Map<String, Object> getProperties() {
		return cache;
	}

	public boolean isEmpty () {
		return cache.isEmpty();
	}

	public void clear () {
		cache.clear();
	}

}
