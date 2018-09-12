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
package org.eclipse.lsp4xml.dom.parser;

import java.util.regex.Pattern;

public class Constants {

	public final static int _BNG = "!".codePointAt(0);
	public final static int _MIN = "-".codePointAt(0);
	public final static int _LAN = "<".codePointAt(0);
	public final static int _RAN = ">".codePointAt(0);
	public final static int _FSL = "/".codePointAt(0);
	public final static int _EQS = "=".codePointAt(0);
	public final static int _DQO = "\"".codePointAt(0);
	public final static int _SQO = "\"".codePointAt(0);
	public final static int _SIQ = "\'".codePointAt(0);
	public final static int _NWL = "\n".codePointAt(0);
	public final static int _CAR = "\r".codePointAt(0);
	public final static int _LFD = "\f".codePointAt(0);
	public final static int _WSP = " ".codePointAt(0);
	public final static int _TAB = "\t".codePointAt(0);
	public final static int _OSB = "[".codePointAt(0);
	public final static int _CSB = "]".codePointAt(0);
	public final static int _CVL = "C".codePointAt(0);
	public final static int _DVL = "D".codePointAt(0);
	public final static int _AVL = "A".codePointAt(0);
	public final static int _TVL = "T".codePointAt(0);
	public final static int _OVL = "O".codePointAt(0);
	public final static int _YVL = "Y".codePointAt(0);
	public final static int _PVL = "P".codePointAt(0);
	public final static int _EVL = "E".codePointAt(0);
	public final static int _QMA = "?".codePointAt(0);
	public final static int _XVL = "x".codePointAt(0);
	public final static int _MVL = "m".codePointAt(0);
	public final static int _LVL = "l".codePointAt(0);

	public static final Pattern ELEMENT_NAME_REGEX = Pattern.compile("^[_:\\w][_:\\w-.\\d]*");

	public static final Pattern ATTRIBUTE_NAME_REGEX = Pattern.compile("^[^\\s\"'<>/=\\x00-\\x0F\\x7F\\x80-\\x9F]*");

	public static final Pattern ATTRIBUTE_VALUE_REGEX = Pattern.compile("^[^\\s\"'`=<>\\/]+");

	public static final Pattern PROLOG_NAME_OPTIONS = Pattern.compile("^(xml|xml-stylesheet)$");

	public static final Pattern PI_TAG_NAME = Pattern.compile("^[a-zA-Z0-9]+");

	// public static final Pattern DOCTYPE_NAME =
	// Pattern.compile("^[_:\\w][_:\\w-.\\d]*");
}
