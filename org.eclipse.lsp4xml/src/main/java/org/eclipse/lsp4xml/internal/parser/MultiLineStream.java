/**
 *  Copyright (c) 2018 Angelo ZERR, Daniel Dekany.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.internal.parser;

import static org.eclipse.lsp4xml.internal.parser.Constants._CAR;
import static org.eclipse.lsp4xml.internal.parser.Constants._LFD;
import static org.eclipse.lsp4xml.internal.parser.Constants._NWL;
import static org.eclipse.lsp4xml.internal.parser.Constants._TAB;
import static org.eclipse.lsp4xml.internal.parser.Constants._WSP;
import static org.eclipse.lsp4xml.internal.parser.Constants._LAN;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Multi line stream.
 *
 */
class MultiLineStream {

	private final String source;
	private final int len;
	private int position;

	public MultiLineStream(String source, int position) {
		this.source = source;
		this.len = source.length();
		this.position = position;
	}

	public boolean eos() {
		return this.len <= this.position;
	}

	public String getSource() {
		return this.source;
	}

	public int pos() {
		return this.position;
	}

	public void goBackTo(int pos) {
		this.position = pos;
	}

	public void goBack(int n) {
		this.position -= n;
	}

	public void advance(int n) {
		this.position += n;
	}

	public void goToEnd() {
		this.position = len;
	}

	/*public int nextChar() {
		// return this.source.codePointAt(this.position++) || 0;
		int next = this.source.codePointAt(this.position++);
		return next >= 0 ? next : 0;
	}*/

	public int peekChar() {
		return peekChar(0);
	}

	public int peekChar(int n) {		
		int pos = this.position + n;
		if (pos >= len ) {
			return -1;
		}
		return this.source.codePointAt(pos);
	}

	public boolean advanceIfChar(int ch) {
		if (ch == peekChar()) {
			this.position++;
			return true;
		}
		return false;
	}

	public boolean advanceIfChars(int... ch) {
		int i;
		if (this.position + ch.length > this.len) {
			return false;
		}
		for (i = 0; i < ch.length; i++) {
			if (peekChar(i) != ch[i]) {
				return false;
			}
		}
		this.advance(i);
		return true;
	}

	public String advanceIfRegExp(Pattern regex) {
		String str = this.source.substring(this.position);
		Matcher match = regex.matcher(str);
		if (match.find()) {
			String s = match.group(0);
			this.position = this.position + match.start() + s.length();
			return s;
		}
		return "";
	}

	public String advanceUntilRegExp(Pattern regex) {
		String str = this.source.substring(this.position);
		/*
		 * TODO let match = str.match(regex); if (match) { this.position = this.position
		 * + match.index!; return match[0]; } else { this.goToEnd(); }
		 */
		return "";
	}

	/**
	 * Advances stream.position no matter what until it hits ch or eof(this.len)
	 * 
	 * @return boolean: was the char found 
	 */
	public boolean advanceUntilChar(int ch) {
		while (this.position < this.len) {
			if (peekChar() == ch) {
				return true;
			}
			this.advance(1);
		}
		return false;
	}

	public boolean advanceUntilAnyOfChars(int... ch) {
		while (this.position < this.len) {
			for(int i = 0; i < ch.length; i ++) {
				if (peekChar() == ch[i]) {
					return true;
				}
			}
			
			this.advance(1);
		}
		return false;
	}

	public boolean advanceUntilCharOrNewTag(int ch) {
		while (this.position < this.len) {
			if (peekChar() == ch || peekChar() == _LAN) {
				return true;
			}
			this.advance(1);
		}
		return false;
	}

	public boolean advanceUntilChars(int... ch) {
		while (this.position + ch.length <= this.len) {
			int i = 0;
			for (; i < ch.length && peekChar(i) == ch[i]; i++) {
			}
			if (i == ch.length) {
				return true;
			}
			this.advance(1);
		}
		this.goToEnd();
		return false;
	}

	/**
	 * Advances until it matches int[] ch OR it hits '<'
	 * If this returns true, peek if next char is '<' to 
	 * check which case was hit
	 */
	public boolean advanceUntilCharsOrNewTag(int... ch) {

		while (this.position + ch.length <= this.len) {
			int i = 0;
			if(peekChar(0) == _LAN) { // <
				return true;
			}
			for (; i < ch.length && peekChar(i) == ch[i]; i++) {
			}
			if (i == ch.length) {
				return true;
			}
			this.advance(1);
		}
		this.goToEnd();
		return false;
	}

	public boolean skipWhitespace() {
		int n = this.advanceWhileChar(ch -> {
			return ch == _WSP || ch == _TAB || ch == _NWL || ch == _LFD || ch == _CAR;
		});
		return n > 0;
	}

	public int advanceWhileChar(Predicate<Integer> condition) {
		int posNow = this.position;
		while (this.position < this.len && condition.test(peekChar())) {
			this.position++;
		}
		return this.position - posNow;
	}
}