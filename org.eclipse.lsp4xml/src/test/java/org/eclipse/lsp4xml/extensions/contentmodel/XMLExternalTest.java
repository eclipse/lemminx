/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.contentmodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4xml.XMLLanguageServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * This class tests the functionality where an XML file undergoes
 * validation against its schema when the XSD schema file (or DTD file)
 * has been modified and saved externally through means, not within 
 * the LS client.
 * 
 * @author David Kwon
 *
 */
public class XMLExternalTest {

	private int threadSleepMs = 600;
	
	private static String tempDirPath = "target/temp/";
	private static URI tempDirUri = Paths.get(tempDirPath).toAbsolutePath().toUri();

	private List<PublishDiagnosticsParams> actualDiagnostics;
	private XMLLanguageServer languageServer;

	@BeforeClass
	public static void setup() throws FileNotFoundException, IOException {
		deleteTempDirIfExists();
		createTempDir();
	}

	@AfterClass
	public static void tearDown() throws IOException {
		deleteTempDirIfExists();
	}

	@Before
	public void before() {
		actualDiagnostics = new ArrayList<>();
		languageServer = createServer(actualDiagnostics);
	}

	private static void deleteTempDirIfExists() throws IOException {
		File tempDir = new File(tempDirUri);
		if (tempDir.exists()) {
			MoreFiles.deleteRecursively(tempDir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
		}
	}

	private static void createTempDir() {
		File tempDir = new File(tempDirUri);
		tempDir.mkdir();
	}

	@Test
	public void externalDTDTest() throws InterruptedException, IOException {
		String dtdPath = tempDirUri.getPath() + "/note.dtd";

		//@formatter:off
		String dtdContents =
		"<!ELEMENT note (to,from,heading,body)>\n" +
		"<!ELEMENT to (#PCDATA)>\n" +
		"<!ELEMENT from (#PCDATA)>\n" +
		"<!ELEMENT heading (#PCDATA)>\n" +
		"<!ELEMENT body (#PCDATA)>";
	
		String xmlContents = 
		"<?xml version=\"1.0\"?>\n" +
		"<!DOCTYPE note SYSTEM \"note.dtd\">\n" +
		"<note>\n" +
		"  <to>Tove</to>\n" +
		"  <from>Jani</from>\n" +
		"  <heading>Reminder</heading>\n" +
		"  <body>Don\'t forget me this weekend!</body>\n" +
		"</note>";
		//@formatter:on

		
		TextDocumentItem xmlTextDocument = getXMLTextDocumentItem("test.xml", xmlContents);
		createFile(dtdPath, dtdContents);
		File testDtd = new File(dtdPath);

		clientOpenFile(languageServer, xmlTextDocument);
	
		Thread.sleep(threadSleepMs);

		Assert.assertEquals(1, actualDiagnostics.size());
		Assert.assertEquals(0, actualDiagnostics.get(0).getDiagnostics().size());

		editFile(testDtd, 2, "");
		didChangedWatchedFiles(languageServer, testDtd);

		Thread.sleep(threadSleepMs);

		Assert.assertEquals(2, actualDiagnostics.size());
		Assert.assertEquals("MSG_ELEMENT_NOT_DECLARED", actualDiagnostics.get(1).getDiagnostics().get(0).getCode());
	}

	@Test
	public void externalXSDTest() throws InterruptedException, IOException {
		String xsdPath = tempDirUri.getPath() + "/sequence.xsd";

		//@formatter:off
		String xsdContents =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
		"<xs:schema\n" +
		"    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
		"    elementFormDefault=\"qualified\">\n" +
		"  <xs:element name=\"root\">\n" +
		"    <xs:complexType>\n" +
		"      <xs:sequence>\n" +
		"        <xs:element name=\"tag\"/>\n" +
		"        <xs:element\n" +
		"            name=\"optional\"\n" +
		"            minOccurs=\"0\"\n" +
		"            maxOccurs=\"3\"/>\n" +
		"      </xs:sequence>\n" +
		"    </xs:complexType>\n" +
		"  </xs:element>\n" +
		"</xs:schema>";
	
		String xmlContents = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
		"<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"sequence.xsd\">\n" +
		"  <tag></tag>\n" +
		"  <optional></optional>\n" +
		"  <optional></optional>\n" +
		"  <optional></optional>\n" +
		"</root>";
		//@formatter:on

		TextDocumentItem xmlTextDocument = getXMLTextDocumentItem("test.xml", xmlContents);
		createFile(xsdPath, xsdContents);
		File testXsd = new File(xsdPath);

		clientOpenFile(languageServer, xmlTextDocument);
	
		Thread.sleep(threadSleepMs);

		Assert.assertEquals(1, actualDiagnostics.size());
		Assert.assertEquals(0, actualDiagnostics.get(0).getDiagnostics().size());

		editFile(testXsd, 12, "            maxOccurs=\"2\"/>");
		didChangedWatchedFiles(languageServer, testXsd);

		Thread.sleep(threadSleepMs);

		Assert.assertEquals(2, actualDiagnostics.size());
		Assert.assertEquals("cvc-complex-type.2.4.f", actualDiagnostics.get(1).getDiagnostics().get(0).getCode());
	}

	private static XMLLanguageServer createServer(List<PublishDiagnosticsParams> actualDiagnostics) {

		XMLLanguageServer languageServer = new XMLLanguageServer();
		LanguageClient client = new LanguageClient() {

			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
				return null;
			}

			@Override
			public void showMessage(MessageParams messageParams) {

			}

			@Override
			public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
				actualDiagnostics.add(diagnostics);
			}

			@Override
			public void logMessage(MessageParams message) {

			}

			@Override
			public void telemetryEvent(Object object) {

			}
		};
		languageServer.setClient(client);
		return languageServer;
	}

	private void createFile(String path, String contents) throws IOException {
		Files.asCharSink(new File(path), Charsets.UTF_8).write(contents);
	}

	private TextDocumentItem getXMLTextDocumentItem(String filename, String xmlContents) {
		String languageId = "xml";
		int version = 1;
		return new TextDocumentItem(tempDirUri.toString() + "/" + filename, languageId, version, xmlContents);
	}

	private void clientOpenFile(XMLLanguageServer languageServer, TextDocumentItem textDocumentItem) {
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(textDocumentItem);
		languageServer.getTextDocumentService().didOpen(params);
	}

	private void editFile(File file, int lineNumber, String newContent) throws IOException {
		List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
		lines.set(lineNumber - 1, newContent);
		java.nio.file.Files.write(file.toPath(), lines);
	}

	private void didChangedWatchedFiles(XMLLanguageServer ls, File file) {
		List<FileEvent> changes = new ArrayList<>();
		changes.add(new FileEvent(file.toURI().toString(), FileChangeType.Changed));
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(changes);
		ls.getWorkspaceService().didChangeWatchedFiles(params);
	}
}