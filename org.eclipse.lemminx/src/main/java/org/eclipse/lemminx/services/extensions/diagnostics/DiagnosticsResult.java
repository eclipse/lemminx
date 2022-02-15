/**
 *  Copyright (c) 2022 Red Hat Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.services.extensions.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Diagnostic;

/**
 * This class is the result of a diagnostic process. It contains:
 *
 * <ul>
 * <li>list of diagnostics.</li>
 * <li>list of completable future which are not done(ex : download some external
 * resources XSD, DTD). This list of future gives the capability to refresh
 * again the diagnostics once all completable futures are finished (ex : all
 * download are finished).</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 */
public class DiagnosticsResult extends ArrayList<Diagnostic> {

	private static final long serialVersionUID = 1L;

	public static final DiagnosticsResult EMPTY;

	static {
		EMPTY = new DiagnosticsResult();
		EMPTY.futures = Collections.emptyList();
	}

	private transient List<CompletableFuture<?>> futures;

	public void addFuture(CompletableFuture<?> future) {
		if (futures == null) {
			futures = new ArrayList<>();
		}
		futures.add(future);
	}

	/**
	 * Returns the completable futures used in a diagnostics (ex : completeable
	 * future to download external resources XSD, DTD) and an empty list otherwise.
	 *
	 * @return the completable futures used in a diagnostics (ex : completeable
	 *         future to download external resources XSD, DTD) and an empty list
	 *         otherwise.
	 */
	public List<CompletableFuture<?>> getFutures() {
		if (futures == null) {
			return Collections.emptyList();
		}
		return futures;
	}

}
