/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.performance;

import static org.eclipse.lsp4xml.utils.IOUtils.convertStreamToString;

import java.io.InputStream;

import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;

/**
 * This utility class is used to check the memory usage of {@link XMLScanner},
 * loading the large nasa.xml file
 * 
 * @author Angelo ZERR
 *
 */
public class XMLScannerPerformance {

	public static void main(String[] args) {
		InputStream in = XMLScannerPerformance.class.getResourceAsStream("/xml/nasa.xml");
		String text = convertStreamToString(in);
		// Continuously parses the large nasa.xml file with the XML scanner
		while (true) {
			long start = System.currentTimeMillis();
			Scanner scanner = XMLScanner.createScanner(text);
			TokenType token = scanner.scan();
			while (token != TokenType.EOS) {
				token = scanner.scan();
			}
			System.err.println("Parsed 'nasa.xml' with XMLScanner in " + (System.currentTimeMillis() - start) + " ms.");
		}
	}
}
