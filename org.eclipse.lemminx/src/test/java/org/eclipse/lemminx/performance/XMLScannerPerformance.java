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
package org.eclipse.lemminx.performance;

import static org.eclipse.lemminx.utils.IOUtils.convertStreamToString;

import java.io.InputStream;

import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lemminx.dom.parser.TokenType;
import org.eclipse.lemminx.dom.parser.XMLScanner;

/**
 * This utility class is used to check the memory usage of {@link XMLScanner},
 * loading the large content.xml file
 * 
 * @author Angelo ZERR
 *
 */
public class XMLScannerPerformance {

	public static void main(String[] args) {
		InputStream in = XMLScannerPerformance.class.getResourceAsStream("/xml/content.xml");
		String text = convertStreamToString(in);
		// Continuously parses the large content.xml file with the XML scanner
		while (true) {
			long start = System.currentTimeMillis();
			Scanner scanner = XMLScanner.createScanner(text);
			TokenType token = scanner.scan();
			while (token != TokenType.EOS) {
				token = scanner.scan();
			}
			System.err.println("Parsed 'content.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
		}
	}
}
