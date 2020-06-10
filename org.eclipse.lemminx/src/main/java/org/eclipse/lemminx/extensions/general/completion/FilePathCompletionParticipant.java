/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lemminx.extensions.general.completion;

import static org.eclipse.lemminx.utils.FilesUtils.getFilePathSlash;
import static org.eclipse.lemminx.utils.OSUtils.isWindows;
import static org.eclipse.lemminx.utils.StringUtils.isEmpty;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.ICompletionResponse;
import org.eclipse.lemminx.utils.CompletionSortTextHelper;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

/**
 * FilePathCompletionParticipant
 */
public class FilePathCompletionParticipant extends CompletionParticipantAdapter {

	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response)
			throws Exception {
		addCompletionItems(valuePrefix, request, response);
	}

	@Override
	public void onDTDSystemId(String valuePrefix, ICompletionRequest request, ICompletionResponse response)
			throws Exception {
		addCompletionItems(valuePrefix, request, response);
	}

	private void addCompletionItems(String valuePrefix, ICompletionRequest request, ICompletionResponse response)
			throws Exception {

		String fullAttributeValue = valuePrefix;
		if (isEmpty(fullAttributeValue)) {
			return;
		}

		DOMDocument xmlDocument = request.getXMLDocument();
		String text = xmlDocument.getText();

		// Get value and range for file path declared inside the attribute value
		int attrValueStartOffset = xmlDocument.offsetAt(request.getReplaceRange().getStart());
		int endPathOffset = request.getOffset(); // offset after the typed character
		int startPathOffset = StringUtils.getOffsetAfterWhitespace(fullAttributeValue,
				endPathOffset - attrValueStartOffset) + attrValueStartOffset; // first character of URI
		String filePathValue = text.substring(startPathOffset, endPathOffset);
		Range filePathRange = XMLPositionUtility.createRange(startPathOffset, endPathOffset, xmlDocument);

		boolean hasFileScheme = filePathValue.startsWith(FilesUtils.FILE_SCHEME);

		if (hasFileScheme) {

			String osSpecificAttributePath = FilesUtils.removeFileScheme(filePathValue);
			if (!osSpecificAttributePath.startsWith("/")) {
				// return; // use of 'file://' and the path was not absolute
			}
			if (isWindows && osSpecificAttributePath.length() == 1) { // only '/', so list Windows Drives

				Range replaceRange = adjustReplaceRange(xmlDocument, filePathRange, filePathValue, "/");

				File[] drives = File.listRoots();
				for (File drive : drives) {
					createFilePathCompletionItem(drive, replaceRange, response, "/");
				}
				return;
			}
		}

		Path validAttributePath = FilesUtils.getPath(filePathValue);
		boolean relativePath = filePathValue.indexOf(":") == -1 || filePathValue.startsWith("\\");
		if (relativePath) {
			Path workingDirectoryPath = FilesUtils.getPath(xmlDocument.getTextDocument().getUri()).getParent();
			validAttributePath = workingDirectoryPath.resolve(validAttributePath).normalize();
		}

		if (!filePathValue.endsWith("/") && !filePathValue.endsWith("\\")) {
			validAttributePath = validAttributePath.getParent();
		}

		if (!Files.exists(validAttributePath)) {
			return;
		}

		String slashInAttribute = getFilePathSlash(filePathValue);
		// Get adjusted range for the completion item (insert at end, or overwrite some
		// existing text in the path)
		Range replaceRange = adjustReplaceRange(xmlDocument, filePathRange, filePathValue, slashInAttribute);
		createNextValidCompletionPaths(validAttributePath, slashInAttribute, replaceRange, response, null);
	}

	/**
	 * Returns a Range that covers trailing content after a slash, or if it already
	 * ends with a slash then a Range right after it.
	 * 
	 * @param xmlDocument
	 * @param fullRange
	 * @param attributeValue
	 * @param slash
	 * @return
	 */
	private Range adjustReplaceRange(DOMDocument xmlDocument, Range fullRange, String attributeValue, String slash) {
		// In the case the currently typed file/directory needs to be overwritten
		Position replaceStart = null;
		Position currentEnd = fullRange.getEnd();

		int startOffset;
		try {
			startOffset = xmlDocument.offsetAt(fullRange.getStart());
		} catch (BadLocationException e) {
			return null;
		}
		int lastSlashIndex = attributeValue.lastIndexOf(slash);
		if (lastSlashIndex > -1) {
			try {
				replaceStart = xmlDocument.positionAt(startOffset + lastSlashIndex);
			} catch (BadLocationException e) {
				return null;
			}
		}
		Range replaceRange = new Range();
		if (replaceStart != null) {
			replaceRange.setStart(replaceStart);
		} else {
			replaceRange.setStart(currentEnd);
		}
		replaceRange.setEnd(currentEnd);

		return replaceRange;
	}

	/**
	 * Creates the completion items based off the given absolute path
	 * 
	 * @param pathToAttributeDirectory
	 * @param attributePath
	 * @param replaceRange
	 * @param response
	 * @param filter
	 */
	private void createNextValidCompletionPaths(Path pathToAttributeDirectory, String slash, Range replaceRange,
			ICompletionResponse response, FilenameFilter filter) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathToAttributeDirectory)) {
			for (Path entry : stream) {
				createFilePathCompletionItem(entry.toFile(), replaceRange, response, slash);
			}
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	private void createFilePathCompletionItem(File f, Range replaceRange, ICompletionResponse response, String slash) {
		CompletionItem item = new CompletionItem();
		String fName = FilesUtils.encodePath(f.getName());
		if (isWindows && fName.isEmpty()) { // Edge case for Windows drive letter
			fName = f.getPath();
			fName = fName.substring(0, fName.length() - 1);
		}
		String insertText;
		insertText = slash + fName;
		item.setLabel(insertText);

		CompletionItemKind kind = f.isDirectory() ? CompletionItemKind.Folder : CompletionItemKind.File;
		item.setKind(kind);

		item.setSortText(CompletionSortTextHelper.getSortText(kind));
		item.setFilterText(insertText);
		item.setTextEdit(new TextEdit(replaceRange, insertText));
		response.addCompletionItem(item);
	}

}