/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.commons.progress;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Progress monitor which wraps the LSP progress API to simplify the
 * implementation of progress.
 * 
 * @author Angelo ZERR
 *
 */
public class ProgressMonitor implements CancelChecker {

	private final String progressId;

	private final ProgressSupport progressSupport;

	private CompletableFuture<Void> future;

	public ProgressMonitor(ProgressSupport progressSupport) {
		this(UUID.randomUUID().toString(), progressSupport);
	}

	public ProgressMonitor(String progressId, ProgressSupport progressSupport) {
		this.progressId = progressId;
		this.progressSupport = progressSupport;

		// Initialize progress
		WorkDoneProgressCreateParams create = new WorkDoneProgressCreateParams(Either.forLeft(progressId));
		future = progressSupport.createProgress(create);
	}

	public void begin(String title, String message, Integer percentage, Boolean cancellable) {
		// Start progress
		WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
		begin.setTitle(title);
		begin.setMessage(message);
		begin.setPercentage(percentage);
		begin.setCancellable(cancellable);
		progressSupport.notifyProgress(progressId, begin);
	}

	public void report(String message, Integer percentage, Boolean cancellable) {
		// report message
		WorkDoneProgressReport report = new WorkDoneProgressReport();
		report.setMessage(message);
		report.setPercentage(percentage);
		report.setCancellable(cancellable);
		progressSupport.notifyProgress(progressId, report);
	}

	public void end(String message) {
		WorkDoneProgressEnd end = new WorkDoneProgressEnd();
		end.setMessage(message);
		progressSupport.notifyProgress(progressId, end);
	}

	@Override
	public void checkCanceled() {
		if (future != null && future.isCancelled()) {
			throw new CancellationException();
		}
	}

	@Override
	public boolean isCanceled() {
		return future != null ? future.isCancelled() : true;
	}

}
