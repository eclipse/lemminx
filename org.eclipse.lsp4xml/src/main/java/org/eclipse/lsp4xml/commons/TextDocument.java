/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.commons;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;

/**
 * Text document extends LSP4j {@link TextDocumentItem} to provide methods to
 * retrieve position.
 *
 */
public class TextDocument extends TextDocumentItem {

	private static String DEFAULT_DELIMTER = System.lineSeparator();

	private ListLineTracker lineTracker;

	// Buffer of the text document used only in incremental mode.
	private StringBuilder buffer;

	public TextDocument(TextDocumentItem document) {
		this(document.getText(), document.getUri());
		super.setVersion(document.getVersion());
		super.setLanguageId(document.getLanguageId());
	}

	public TextDocument(String text, String uri) {
		super.setUri(uri);
		super.setText(text);
	}

	public void setIncremental(boolean incremental) {
		if (incremental) {
			buffer = new StringBuilder(getText());
		} else {
			buffer = null;
		}
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		lineTracker = null;
	}

	public Position positionAt(int position) throws BadLocationException {
		ListLineTracker lineTracker = getLineTracker();
		return lineTracker.getPositionAt(position);
	}

	public int offsetAt(Position position) throws BadLocationException {
		ListLineTracker lineTracker = getLineTracker();
		return lineTracker.getOffsetAt(position);
	}

	public String lineText(int lineNumber) throws BadLocationException {
		ListLineTracker lineTracker = getLineTracker();
		Line line = lineTracker.getLineInformation(lineNumber);
		String text = super.getText();
		return text.substring(line.offset, line.offset + line.length);
	}

	public String lineDelimiter(int lineNumber) throws BadLocationException {
		ListLineTracker lineTracker = getLineTracker();
		String lineDelimiter = lineTracker.getLineDelimiter(lineNumber);
		if (lineDelimiter == null) {
			if (lineTracker.getNumberOfLines() > 0) {
				lineDelimiter = lineTracker.getLineInformation(0).delimiter;
			}
		}
		if (lineDelimiter == null) {
			lineDelimiter = DEFAULT_DELIMTER;
		}
		return lineDelimiter;
	}

	public Range getWordRangeAt(int textOffset, Pattern wordDefinition) {
		try {
			Position pos = positionAt(textOffset);
			ListLineTracker lineTracker = getLineTracker();
			Line line = lineTracker.getLineInformation(pos.getLine());
			String text = super.getText();
			String lineText = text.substring(line.offset, textOffset);
			int position = lineText.length();
			Matcher m = wordDefinition.matcher(lineText);
			int currentPosition = 0;
			while (currentPosition != position) {
				if (m.find()) {
					currentPosition = m.end();
					if (currentPosition == position) {
						return new Range(new Position(pos.getLine(), m.start()), pos);
					}
				} else {
					currentPosition++;
				}
				m.region(currentPosition, position);
			}
			return new Range(pos, pos);
		} catch (BadLocationException e) {
			return null;
		}
	}

	private ListLineTracker getLineTracker() {
		if (lineTracker == null) {
			lineTracker = new ListLineTracker();
			lineTracker.set(super.getText());
		}
		return lineTracker;
	}

	/**
	 * Update text of the document by using the changes and according the
	 * incremental support.
	 * 
	 * @param changes the text document changes.
	 */
	public void update(List<TextDocumentContentChangeEvent> changes) {
		if (changes.size() < 1) {
			// no changes, ignore it.
			return;
		}
		if (isIncremental()) {
			try {
				synchronized (buffer) {
					for (TextDocumentContentChangeEvent changeEvent : changes) {

						Range range = changeEvent.getRange();
						int length = 0;

						if (range != null) {
							length = changeEvent.getRangeLength().intValue();
						} else {
							// range is optional and if not given, the whole file content is replaced
							length = getText().length();
							range = new Range(positionAt(0), positionAt(length));
						}
						String text = changeEvent.getText();
						int startOffset = offsetAt(range.getStart());
						buffer.replace(startOffset, startOffset + length, text);
					}
					setText(buffer.toString());
				}
			} catch (BadLocationException e) {
				// Should never occurs.
			}
		} else {
			// like vscode does, get the last changes
			// see
			// https://github.com/Microsoft/vscode-languageserver-node/blob/master/server/src/main.ts
			TextDocumentContentChangeEvent last = changes.size() > 0 ? changes.get(changes.size() - 1) : null;
			if (last != null) {
				setText(last.getText());
			}
		}
	}

	public boolean isIncremental() {
		return buffer != null;
	}

}
