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
import java.util.Map;

/**
 * Store telemetry data for transmission at a later time
 */
public class TelemetryCache {

	private Map<String, Integer> cache = new HashMap<>();

	public void put (String key) {
		cache.put(key, cache.getOrDefault(key, 0) + 1);
	}

	public Map<String, Integer> getProperties() {
		return cache;
	}

	public boolean isEmpty () {
		return cache.isEmpty();
	}

	public void clear () {
		cache.clear();
	}

}
