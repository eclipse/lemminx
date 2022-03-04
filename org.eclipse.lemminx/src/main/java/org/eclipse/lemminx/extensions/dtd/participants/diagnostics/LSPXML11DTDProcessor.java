/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.dtd.participants.diagnostics;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dtd.XML11DTDProcessor;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.eclipse.lemminx.extensions.xerces.LSPXMLEntityManager;

/**
 * The Xerces {@link XML11DTDProcessor} doesn't give the capability to customize
 * the XML entity manager.
 * 
 * <p>
 * This class extends {@link XML11DTDProcessor} and customizes the XML entity
 * manager with the {@link LSPXMLEntityManager} which handles the remote
 * resource download errors.
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class LSPXML11DTDProcessor extends XML11DTDProcessor {

	public LSPXML11DTDProcessor(XMLEntityManager entityManager, XMLErrorReporter errorReporter,
			XMLEntityResolver entityResolver) {
		super();
		fEntityManager = entityManager;
		fErrorReporter = errorReporter;
		// Add XML message formatter if there isn't one.
		if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
			XMLMessageFormatter xmft = new XMLMessageFormatter();
			fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
			fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
		}
		fEntityResolver = entityResolver;
		fEntityManager.setProperty(Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY, errorReporter);
		fDTDScanner = createDTDScanner(fSymbolTable, fErrorReporter, fEntityManager);
		fDTDScanner.setDTDHandler(this);
		fDTDScanner.setDTDContentModelHandler(this);
		reset();
	}
}
