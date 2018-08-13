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

package org.eclipse.lsp4xml.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.lsp4j.services.LanguageClient;

/**
 * LogHelper
 */
public class LogHelper {
	private static String logPath;
	private static String defaultLogPath;
	private static boolean shouldLogToClient = true;

	// This will apply to all child loggers
	public static void initializeRootLogger(LanguageClient newLanguageClient) {
		Logger logger = Logger.getLogger("");
		unregisterAllHandlers(logger.getHandlers());
		logger.setLevel(Level.SEVERE);
		logger.setUseParentHandlers(false);

		if(newLanguageClient == null || logPath == null) {
			logger.info("Parameter(s) given to LogHelper.initializeRootLogger were null");
			return;
		}
		
		if(shouldLogToClient) {
			try {
					logger.addHandler(LogHelper.getClientHandler(newLanguageClient));	
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		createDirectoryPath(logPath);
		try {
			FileHandler fh = LogHelper.getFileHandler(logPath);
			logger.addHandler(fh);
			
		} catch (SecurityException | IOException e) {
			logger.log(Level.WARNING, "Error at creation of FileHandler for logging");
		}
		

	}

	/**
	 * Creates the parent directory path to the file if it does not already exist
	 */
	private static void createDirectoryPath(String path) {
		Path currentPath = Paths.get(path);
		Path directoryPath = currentPath.normalize().getParent();
		String parentPath;
		if(directoryPath != null) {
			parentPath = currentPath.normalize().getParent().toString();
			new File(parentPath).mkdirs();
		} else {
			logPath = currentPath.normalize().toString();
		}
		
		
	}

	public static ClientLogHandler getClientHandler(LanguageClient languageClient) {
		if(languageClient == null) {
			return null;
		}
		return new ClientLogHandler(languageClient);
	}

	public static FileHandler getFileHandler(String filePath) throws SecurityException, IOException {
		if(filePath == null ||filePath.isEmpty()) {
			throw new IllegalArgumentException("Incorrect file path provided");
		}
		File f = new File(filePath);
		if(f.isDirectory()) {
			throw new IllegalArgumentException("Provided path was a directory");
		}
		if(!f.exists() || f.canWrite()) {
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
		if(handlers == null) {
			return;
		}
		for (Handler h : handlers) {
			unregisterHandler(h);
			;
		}
	}

	public static void updatePath(String newLogPath) {
		if(newLogPath == null) {
			return;
		}
		if(newLogPath.equals("default") || newLogPath.equals("")) {
			if(defaultLogPath == null || defaultLogPath == "") {
				defaultLogPath = "./badPathLogFile.log";
			}
			logPath = defaultLogPath;
		}
		else{
			LogHelper.logPath = newLogPath;
		}
	}

	public static void setDefaultLogPath(String newDefaultLogPath) {
		defaultLogPath = newDefaultLogPath;
	}

	public static void setShouldLogToClient(boolean b) {
		shouldLogToClient = b;
	}
}