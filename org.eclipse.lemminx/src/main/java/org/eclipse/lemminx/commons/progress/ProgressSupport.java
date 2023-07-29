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

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressNotification;

/**
 * LSP Progress support API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ProgressSupport {

	default ProgressMonitor createProgressMonitor() {
		if (!isWorkDoneProgressSupported()) {
			return null;
		}
		return new ProgressMonitor(this);
	}

	default ProgressMonitor createProgressMonitor(String progressId) {
		if (!isWorkDoneProgressSupported()) {
			return null;
		}
		return new ProgressMonitor(progressId, this);
	}

	/**
	 * Returns true if the LSP client can support LSP progress and false otherwise.
	 * 
	 * @return true if the LSP client can support LSP progress and false otherwise.
	 */
	boolean isWorkDoneProgressSupported();

	/**
	 * Create a progress.
	 * 
	 * @param params the progress create parameters
	 * @return
	 */
	CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params);

	/**
	 * Notify the the progress notification with the given process id.
	 * 
	 * @param progressId   the progress id.
	 * @param notification the progress notification.
	 */
	void notifyProgress(String progressId, WorkDoneProgressNotification notification);
}