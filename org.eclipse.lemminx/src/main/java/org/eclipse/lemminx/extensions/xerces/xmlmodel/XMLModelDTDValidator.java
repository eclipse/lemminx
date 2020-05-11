/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.xerces.xmlmodel;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * XML model validator which process validation with DTD:
 * 
 * <pre>
 * 	&lt;?xml-model href="http://java.sun.com/dtd/web-app_2_3.dtd"?&gt;
 * </pre>
 *
 */
public class XMLModelDTDValidator extends XMLDTDValidator implements XMLModelValidator {

	private static final String ENTITY_MANAGER = Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;

	private String href;
	private boolean rootElement;
	private XMLLocator locator;
	private XMLEntityManager entityManager;

	public XMLModelDTDValidator() {
		rootElement = true;
		fDTDValidation = true;
	}

	@Override
	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public void setLocator(XMLLocator locator) {
		this.locator = locator;
	}

	@Override
	public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
		if (rootElement) {
			QName fRootElement = getRootElement();
			String rootElementName = element.localpart;

			// save root element state
			fSeenDoctypeDecl = true;
			fRootElement.setValues(null, rootElementName, rootElementName, null);

			String eid = null;
			try {
				eid = XMLEntityManager.expandSystemId(href, locator.getExpandedSystemId(), false);
			} catch (java.io.IOException e) {
			}
			XMLDTDDescription grammarDesc = new XMLDTDDescription(null, href, locator.getExpandedSystemId(), eid,
					rootElementName);
			fDTDGrammar = fGrammarBucket.getGrammar(grammarDesc);
			if (fDTDGrammar == null) {
				// give grammar pool a chance...
				//
				// Do not bother checking the pool if no public or system identifier was
				// provided.
				// Since so many different DTDs have roots in common, using only a root name as
				// the
				// key may cause an unexpected grammar to be retrieved from the grammar pool.
				// This scenario
				// would occur when an ExternalSubsetResolver has been queried and the
				// XMLInputSource returned contains an input stream but no external identifier.
				// This can never happen when the instance document specified a DOCTYPE. --
				// mrglavas
				if (fGrammarPool != null && (href != null)) {
					fDTDGrammar = (DTDGrammar) fGrammarPool.retrieveGrammar(grammarDesc);
				}
			}
			if (fDTDGrammar == null) {

				XMLDTDLoader loader = new XMLDTDLoader(fSymbolTable, fGrammarPool);
				loader.setEntityResolver(entityManager);
				try {
					fDTDGrammar = (DTDGrammar) loader.loadGrammar(new XMLInputSource(null, eid, null));
				} catch (IOException e) {
					// TODO : manage report error for DTD not found in xml-model/@ref
				}
			} else {
				// we've found a cached one;so let's make sure not to read
				// any external subset!
				fValidationManager.setCachedDTD(true);
			}
			rootElement = false;
		}
		super.startElement(element, attributes, augs);
	}

	private QName getRootElement() {
		try {
			// fRootElement is declared as private in the XMLDTDValidator, we must use ugly
			// Java reflection to get the field.
			Field f = XMLDTDValidator.class.getDeclaredField("fRootElement");
			f.setAccessible(true);
			QName fRootElement = (QName) f.get(this);
			return fRootElement;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
		entityManager = (XMLEntityManager) componentManager.getProperty(ENTITY_MANAGER);
		super.reset(componentManager);
	}
}
