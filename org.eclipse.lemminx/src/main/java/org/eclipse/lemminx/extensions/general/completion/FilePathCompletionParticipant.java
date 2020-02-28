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

import static org.eclipse.lemminx.utils.FilesUtils.convertToWindowsPath;
import static org.eclipse.lemminx.utils.FilesUtils.getFilePathSlash;
import static org.eclipse.lemminx.utils.FilesUtils.getNormalizedPath;
import static org.eclipse.lemminx.utils.OSUtils.isWindows;
import static org.eclipse.lemminx.utils.StringUtils.isEmpty;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.nio.file.Path;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.ICompletionResponse;
import org.eclipse.lemminx.utils.CompletionSortTextHelper;
import org.eclipse.lemminx.utils.FilesUtils;
import org.eclipse.lemminx.utils.StringUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

/**
 * FilePathCompletionParticipant
 */
public class FilePathCompletionParticipant extends CompletionParticipantAdapter {

	public static final String FILE_SCHEME = "file";

	@Override
	public void onAttributeValue(String valuePrefix,
			ICompletionRequest request, ICompletionResponse response) throws Exception {

		DOMDocument xmlDocument = request.getXMLDocument();
		String text = xmlDocument.getText();
		Range fullRange = request.getReplaceRange();
		
		// Get full attribute value range
		int documentStartOffset = xmlDocument.offsetAt(fullRange.getStart());
		
		String fullAttributeValue = valuePrefix;
		if (isEmpty(fullAttributeValue)) {
			return;
		}

		// Get value and range from fullAttributeValue
		int completionOffset = request.getOffset(); // offset after the typed character
		int parsedAttributeStartOffset = StringUtils.getOffsetAfterWhitespace(fullAttributeValue, completionOffset - documentStartOffset) + documentStartOffset; // first character of URI
		String attributePath = text.substring(parsedAttributeStartOffset, completionOffset);
	
		Position startValue = xmlDocument.positionAt(parsedAttributeStartOffset);
		Position endValue = xmlDocument.positionAt(completionOffset);
		fullRange = new Range(startValue, endValue);

		// Try to get the URI string from the attribute value in case it has a file scheme
		// header (eg: "file://")
		String osSpecificAttributePath = attributePath;
		boolean hasFileScheme = false;

		hasFileScheme = attributePath.startsWith(FilesUtils.FILE_SCHEME);
		if (hasFileScheme) {
			osSpecificAttributePath = attributePath.substring(FilesUtils.FILE_SCHEME.length());
		}

		String slashInAttribute = getFilePathSlash(attributePath);

		if (hasFileScheme) {
			if (!osSpecificAttributePath.startsWith("/")) {
				return; // use of 'file://' and the path was not absolute
			}
			if (isWindows && osSpecificAttributePath.length() == 1) { // only '/', so list Windows Drives
				
				Range replaceRange = adjustReplaceRange(xmlDocument, fullRange, attributePath, "/");

				File[] drives = File.listRoots();
				for (File drive : drives) {
					createFilePathCompletionItem(drive, replaceRange, response, "/");
				}
				return;
			}
		}
		
		if(isWindows) {
			osSpecificAttributePath = convertToWindowsPath(osSpecificAttributePath);
		}
		else if("\\".equals(slashInAttribute)) { // Backslash used in Unix
			osSpecificAttributePath = osSpecificAttributePath.replace("\\", "/");
		}
		
		// Get the normalized URI string from the parent directory file if necessary
		String workingDirectory = null; // The OS specific path for a working directory

		if (!hasFileScheme) { //The path from the attribute value is not a uri, so we might need to reference the working directory path
			String uriString = xmlDocument.getTextDocument().getUri();
			URI uri = new URI(uriString);

			if(!FILE_SCHEME.equals(uri.getScheme())) {
				return;
			}

			String uriPathString = uri.getPath();
			if(!uriPathString.startsWith("/")) {
				return; //file uri is incorrect
			}
			int lastSlash = uriPathString.lastIndexOf("/");
			if(lastSlash > -1) {
				workingDirectory = uriPathString.substring(0, lastSlash);

				if(isWindows) {
					// Necessary, so that this path is readable in Windows
					workingDirectory = convertToWindowsPath(workingDirectory);
				}
			}
		}
		
		//Try to get a correctly formatted path from the given values
		Path validAttributeValuePath = getNormalizedPath(workingDirectory, osSpecificAttributePath); 
		
		if(validAttributeValuePath == null) {
			return;
		}

		//Get adjusted range for the completion item (insert at end, or overwrite some existing text in the path)
		Range replaceRange = adjustReplaceRange(xmlDocument, fullRange, attributePath, slashInAttribute);

		createNextValidCompletionPaths(validAttributeValuePath, slashInAttribute, replaceRange, response, null);
	}

	/**
	 * Returns a Range that covers trailing content after a slash, or 
	 * if it already ends with a slash then a Range right after it.
	 * @param xmlDocument
	 * @param fullRange
	 * @param attributeValue
	 * @param slash
	 * @return
	 */
	private Range adjustReplaceRange(DOMDocument xmlDocument, Range fullRange, String attributeValue, String slash) {
		//In the case the currently typed file/directory needs to be overwritten
		Position replaceStart = null;
		Position currentEnd = fullRange.getEnd();
		
		int startOffset;
		try {
			startOffset = xmlDocument.offsetAt(fullRange.getStart());
		} catch (BadLocationException e) {
			return null;
		}
		int lastSlashIndex = attributeValue.lastIndexOf(slash);
		if(lastSlashIndex > -1) {
			try {
				replaceStart = xmlDocument.positionAt(startOffset + lastSlashIndex);
			} catch (BadLocationException e) {
				return null;
			}
		}
		Range replaceRange = new Range();
		if(replaceStart != null) {
			replaceRange.setStart(replaceStart);
		}
		else {
			replaceRange.setStart(currentEnd);
		}
		replaceRange.setEnd(currentEnd);

		return replaceRange;
	}

	/**
	 * Creates the completion items based off the given absolute path
	 * @param pathToAttributeDirectory
	 * @param attributePath
	 * @param replaceRange
	 * @param response
	 * @param filter
	 */
	private void createNextValidCompletionPaths(Path pathToAttributeDirectory, String slash, Range replaceRange, ICompletionResponse response,
			FilenameFilter filter) {

		File[] proposedFiles = gatherFiles(pathToAttributeDirectory, filter);
		if (proposedFiles != null) {
			for (File child : proposedFiles) {
				if (child != null) {
					createFilePathCompletionItem(child, replaceRange, response, slash);
				}
			}
		}
	}

	/**
	 * Returns a list of File objects that are in the given directory
	 * @param pathOfDirectory
	 * @param filter
	 * @return
	 */
	private File[] gatherFiles(Path pathOfDirectory, FilenameFilter filter) {
		File f = new File(pathOfDirectory.toString());
		return f.isDirectory() ? f.listFiles(filter) : null;
	}

	private void createFilePathCompletionItem(File f, Range replaceRange, ICompletionResponse response, String slash) {
		CompletionItem item = new CompletionItem();
		String fName = f.getName();
		if(isWindows && fName.isEmpty()) { // Edge case for Windows drive letter
			fName = f.getPath();
			fName = fName.substring(0, fName.length() - 1);
		}
		String insertText;
		insertText = slash + fName;
		item.setLabel(insertText);

		CompletionItemKind kind = f.isDirectory()? CompletionItemKind.Folder : CompletionItemKind.File;
		item.setKind(kind);
		
		item.setSortText(CompletionSortTextHelper.getSortText(kind));
		item.setFilterText(insertText);
		item.setTextEdit(new TextEdit(replaceRange, insertText));
		response.addCompletionItem(item);
	}

}