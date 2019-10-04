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

import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;

/**
 * Custom Xerces XML parser configuration to :
 * 
 * <ul>
 * <li>disable only DTD validation if required</li>
 * </ul>
 *
 */
class LSPXMLParserConfiguration extends XIncludeAwareParserConfiguration {

	private final boolean disableDTDValidation;

	public LSPXMLParserConfiguration(boolean disableDTDValidation, XMLValidationSettings validationSettings) {
		this.disableDTDValidation = disableDTDValidation;
		// Disable DOCTYPE declaration if settings is set to true.
		boolean disallowDocTypeDecl = validationSettings != null ? validationSettings.isDisallowDocTypeDecl() : false;
		super.setFeature("http://apache.org/xml/features/disallow-doctype-decl", disallowDocTypeDecl);
		// Resolve external entities if settings is set to true.
		boolean resolveExternalEntities = validationSettings != null ? validationSettings.isResolveExternalEntities()
				: false;
		super.setFeature("http://xml.org/sax/features/external-general-entities", resolveExternalEntities);
		super.setFeature("http://xml.org/sax/features/external-parameter-entities", resolveExternalEntities);
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
