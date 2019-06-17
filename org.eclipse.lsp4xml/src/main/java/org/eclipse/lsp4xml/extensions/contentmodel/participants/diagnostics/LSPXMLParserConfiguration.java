/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;

/**
 * Custom Xerces XML parser configuration to :
 * 
 * <ul>
 * <li>disable only DTD validation if required</li>
 * </ul>
 *
 */
class LSPXMLParserConfiguration extends XIncludeAwareParserConfiguration {

	private static final String XML_SCHEMA_VERSION = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.XML_SCHEMA_VERSION_PROPERTY;

	private static final String SCHEMA_VALIDATOR = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.SCHEMA_VALIDATOR_PROPERTY;

	private final String namespaceSchemaVersion;

	private final boolean disableDTDValidation;

	public LSPXMLParserConfiguration(String namespaceSchemaVersion, boolean disableDTDValidation) {
		this.namespaceSchemaVersion = namespaceSchemaVersion;
		this.disableDTDValidation = disableDTDValidation;
	}

	@Override
	protected void configurePipeline() {
		super.configurePipeline();
		configureSchemaVersion();
	}

	@Override
	protected void configureXML11Pipeline() {
		super.configureXML11Pipeline();
		configureSchemaVersion();
	}

	private void configureSchemaVersion() {
		if (namespaceSchemaVersion != null) {
			XMLSchemaValidator validator = (XMLSchemaValidator) super.getProperty(SCHEMA_VALIDATOR);
			if (validator != null) {
				validator.setProperty(XML_SCHEMA_VERSION, namespaceSchemaVersion);
			}
		}
	}

	@Override
	protected void reset() throws XNIException {
		super.reset();
		if (disableDTDValidation) {
			// reset again DTD validator by setting "http://xml.org/sax/features/validation"
			// to false.
			disableDTDValidation();
		}
	}

	private void disableDTDValidation() {
		XMLDTDValidator validator = (XMLDTDValidator) super.getProperty(DTD_VALIDATOR);
		if (validator != null) {
			// Calling XMLDTDValidator#setFeature("http://xml.org/sax/features/validation",
			// false) does nothing.
			// The only way to set "http://xml.org/sax/features/validation" to false is to
			// call XMLDTDValidator#reset with the proper component.
			// We need to create a new component and not use the current configuration
			// otherwise set
			// "http://xml.org/sax/features/validation" to the configuration
			// will update the other component and will disable validation too for XML
			// Schema
			XMLComponentManager disableDTDComponent = new XMLComponentManager() {

				@Override
				public Object getProperty(String propertyId) throws XMLConfigurationException {
					return LSPXMLParserConfiguration.this.getProperty(propertyId);
				}

				@Override
				public boolean getFeature(String featureId) throws XMLConfigurationException {
					if ("http://xml.org/sax/features/validation".equals(featureId)) {
						return false;
					}
					return LSPXMLParserConfiguration.this.getFeature(featureId);
				}
			};
			validator.reset(disableDTDComponent);
		}
	}

}
