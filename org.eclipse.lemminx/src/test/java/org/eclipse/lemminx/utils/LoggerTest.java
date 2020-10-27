/**
 *  Copyright (c) 2018 Red Hat, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Nikolas Komonen <nikolaskomonen@gmail.com>, Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lemminx.utils;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.lemminx.MockXMLLanguageClient;
import org.eclipse.lemminx.logs.LSPClientLogHandler;
import org.eclipse.lemminx.logs.LogHelper;
import org.eclipse.lemminx.settings.LogsSettings;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * LoggerTest
 */
public class LoggerTest {

	private String path = "target/logs/testLogFile.log";
	private MockXMLLanguageClient mockLanguageClient;
	private File logFile;
	private Logger LOGGER = Logger.getLogger(LoggerTest.class.getName());
	private TimeZone originalTimeZone = TimeZone.getDefault();

	@BeforeEach
	public void startup() {
		TimeZone.setDefault(TimeZone.getTimeZone(("UTC")));
		deleteLogFile();
		mockLanguageClient = new MockXMLLanguageClient(); // (MessageType.Error, "Log Message");
		LogsSettings settings = new LogsSettings();
		// Enable log file
		settings.setFile(path);
		// Enable client
		settings.setClient(true);
		LogHelper.initializeRootLogger(mockLanguageClient, settings);
		logFile = new File(path);
	}

	@AfterEach
	public void cleanUp() {
		Handler[] handlers = Logger.getLogger("").getHandlers();
		LogHelper.unregisterAllHandlers(handlers);
		deleteLogFile();
		TimeZone.setDefault(originalTimeZone);
	}

	@Test
	public void testLogCreated() throws IOException {
		String msg = "@@Log Message@@";
		LOGGER.severe(msg);
		assertTrue(logFile.exists());
		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				if (line.contains(msg)) {
					return;
				}
			}
		}
		fail("Log Message wasn't written to file");
	}

	@Test
	public void testClientReceivesLog() {
		LOGGER.severe("@@Log Message@@");
		assertFalse(mockLanguageClient.getLogMessages().isEmpty());
		MessageParams message = mockLanguageClient.getLogMessages().get(0);
		assertEquals(MessageType.Error, message.getType());
		assertTrue(message.getMessage().endsWith("@@Log Message@@"));
	}

	@Test
	public void assertTestFormatting() throws IOException {

		Level level = Level.SEVERE;
		long recordMillis = 874400705000L;
		String recordSourceClassName = "org.my.test.Class";
		String recordSourceMethodName = "mySourceMethod";
		String recordMessage = "Formatting Log Message";
		Throwable throwable = new Throwable() {
			public void printStackTrace(PrintWriter s) {
				StackTraceElement[] trace = getStackTrace();
				for (StackTraceElement traceElement : trace) {
					s.println("\tat " + traceElement);
				}
			}
		};
		StackTraceElement recordStackTrace1 = new StackTraceElement("declaringClass", "methodName", "fileName.java", 1);
		StackTraceElement recordStackTrace2 = new StackTraceElement("declaringClass2", "methodName2.drl.java",
				"fileName2.java", 2);
		StackTraceElement recordStackTrace3 = new StackTraceElement("declaringClass", "methodName.apk.java", "fileName",
				3);
		StackTraceElement[] recordStackTrace = { recordStackTrace1, recordStackTrace2, recordStackTrace3 };
		throwable.setStackTrace(recordStackTrace);

		LogRecord record = new LogRecord(level, recordMessage);

		record.setMillis(recordMillis);
		record.setSourceClassName(recordSourceClassName);
		record.setSourceMethodName(recordSourceMethodName);
		record.setMessage(recordMessage);
		record.setThrown(throwable);
		String expectedOutput = "Sep 16, 1997 09:05:05 org.my.test.Class mySourceMethod()" + lineSeparator()
				+ "Message: Formatting Log Message" + lineSeparator()
				+ "\tat declaringClass.methodName(fileName.java:1)" + lineSeparator()
				+ "\tat declaringClass2.methodName2.drl.java(fileName2.java:2)" + lineSeparator()
				+ "\tat declaringClass.methodName.apk.java(fileName:3)" + lineSeparator();

		assertEquals(expectedOutput, LSPClientLogHandler.formatRecord(record, Locale.US));

	}

	// ---------------------Tools-------------------------------

	private void deleteLogFile() {
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
	}

}