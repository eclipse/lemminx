/**
 *  Copyright (c) 2018 Red Hat, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Nikolas Komonen <nikolaskomonen@gmail.com>, Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.logs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4xml.settings.LogsSettings;

/**
 * LogHelper
 */
public class LogHelper {

	// This will apply to all child loggers
	public static void initializeRootLogger(LanguageClient newLanguageClient, LogsSettings settings) {
		if (newLanguageClient == null || settings == null) {
			return;
		}

		Logger logger = Logger.getLogger("");
		unregisterAllHandlers(logger.getHandlers());
		logger.setLevel(Level.INFO);
		logger.setUseParentHandlers(false);// Stops output to console

		// Configure logging LSP client handler
		if (settings.getClient()) {
			try {
				logger.addHandler(LogHelper.getClientHandler(newLanguageClient));
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		// Configure logging for file
		String path = settings.getFile();
		if (path != null) {
			createDirectoryPath(path);
			try {
				FileHandler fh = LogHelper.getFileHandler(path);
				logger.addHandler(fh);

			} catch (SecurityException | IOException e) {
				logger.log(Level.WARNING, "Error at creation of FileHandler for logging");
			}
		} else {
			logger.log(Level.INFO, "Log file could not be created, path not provided");
		}

	}

	private static void createDirectoryPath(String path) {
		Path parentPath = Paths.get(path).normalize().getParent();
		if (parentPath != null) {
			try {
				Files.createDirectories(parentPath);
			} catch (IOException e) {

			}
		}
	}

	public static LSPClientLogHandler getClientHandler(LanguageClient languageClient) {
		if (languageClient == null) {
			return null;
		}
		return new LSPClientLogHandler(languageClient);
	}

	public static FileHandler getFileHandler(String filePath) throws SecurityException, IOException {
		if (filePath == null || filePath.isEmpty()) {
			throw new IllegalArgumentException("Incorrect file path provided");
		}
		File f = new File(filePath);
		if (f.isDirectory()) {
			throw new IllegalArgumentException("Provided path was a directory");
		}
		if (!f.exists() || f.canWrite()) {
			FileHandler fh = null;
			fh = new FileHandler(filePath, true);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.INFO);
			return fh;
		}
		throw new IOException("Cannot write file since it cannot be written to");
	}

	public static void unregisterHandler(Handler handler) {
		if (handler == null) {
			return;
		}
		handler.close();
		Logger.getLogger("").removeHandler(handler);
	}

	public static void unregisterAllHandlers(Handler[] handlers) {
		if (handlers == null) {
			return;
		}
		for (Handler h : handlers) {
			unregisterHandler(h);
			;
		}
	}
}