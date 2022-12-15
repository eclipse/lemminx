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
package org.eclipse.lemminx.extensions.relaxng.jing;

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.ErrorHandlerProxy;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.extensions.relaxng.jing.toremove.MyCompactSchemaReader;
import org.eclipse.lemminx.extensions.relaxng.jing.toremove.MySAXSchemaReader;
import org.eclipse.lemminx.extensions.relaxng.jing.toremove.MySchemaReaderImpl;
import org.eclipse.lemminx.extensions.relaxng.xml.validator.RelaxNGErrorHandler;
import org.eclipse.lemminx.utils.DOMUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * RelaxNG schema provider.
 *
 * @author Angelo ZERR
 *
 */
public class SchemaProvider {

	private SchemaProvider() {
	}

	public static Schema getSchema(String systemId, String baseSystemId, XMLEntityResolver entityResolver,
			XMLErrorReporter errorReporter, XMLGrammarPool pool)
			throws XNIException, IOException, SAXException, IncorrectSchemaException {
		RelaxNGDescription description = new RelaxNGDescription(systemId, baseSystemId);
		if (pool != null) {
			RelaxNGGrammar grammar = (RelaxNGGrammar) pool.retrieveGrammar(description);
			if (grammar != null) {
				return grammar.getSchema();
			}
		}
		Schema schema = loadSchema(description, entityResolver, errorReporter, null);
		if (pool != null) {
			RelaxNGGrammar grammar = new RelaxNGGrammar(schema, description);
			pool.cacheGrammars(description.getGrammarType(), new Grammar[] { grammar });
		}
		return schema;
	}

	public static Schema loadSchema(RelaxNGDescription description, XMLEntityResolver entityResolver,
			XMLErrorReporter errorReporter, SchemaPatternBuilder schemaPatternBuilder)
			throws MalformedURIException, IOException, SAXException, IncorrectSchemaException {
		InputSource input = createInputSource(description, entityResolver);
		return loadSchema(input, entityResolver, errorReporter, schemaPatternBuilder, null);
	}

	public static Schema loadSchema(InputSource input, XMLEntityResolver entityResolver,
			XMLErrorReporter errorReporter, SchemaPatternBuilder schemaPatternBuilder,
			XMLReaderCreator xmlReaderCreator)
			throws IOException, SAXException, IncorrectSchemaException {
		SchemaReader schemaReader = getSchemaReader(input.getSystemId());
		PropertyMap schemaProperties = createPropertyMap(entityResolver, errorReporter, schemaPatternBuilder,
				xmlReaderCreator);
		return schemaReader.createSchema(new SAXSource(input), schemaProperties);
	}

	private static InputSource createInputSource(RelaxNGDescription description, XMLEntityResolver entityResolver)
			throws MalformedURIException, IOException {
		XMLInputSource source = entityResolver.resolveEntity(description);
		return source.getByteStream() != null ? new InputSource(source.getByteStream())
				: new InputSource(source.getSystemId());
	}

	private static SchemaReader getSchemaReader(String systemId) {
		return DOMUtils.isRelaxNGUriCompactSyntax(systemId) ? MyCompactSchemaReader.getInstance()
				: MySAXSchemaReader.getInstance();
	}

	private static PropertyMap createPropertyMap(XMLEntityResolver entityResolver, XMLErrorReporter errorReporter,
			SchemaPatternBuilder schemaPatternBuilder, XMLReaderCreator xmlReaderCreator) {
		PropertyMapBuilder mapBuilder = new PropertyMapBuilder();
		if (errorReporter != null) {
			mapBuilder.put(ValidateProperty.ERROR_HANDLER, createErrorHandler(errorReporter));
		}
		if (entityResolver != null) {
			mapBuilder.put(ValidateProperty.ENTITY_RESOLVER, new EntityResolver() {

				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					XMLResourceIdentifier identifier = new XMLResourceIdentifierImpl(publicId, systemId, systemId,
							systemId);
					XMLInputSource source = entityResolver.resolveEntity(identifier);
					return new InputSource(source.getByteStream());
				}
			});
		}
		if (schemaPatternBuilder != null) {
			mapBuilder.put(MySchemaReaderImpl.SCHEMA_PATTERN_BUILDER, schemaPatternBuilder);
		}
		if (xmlReaderCreator != null) {
			mapBuilder.put(ValidateProperty.XML_READER_CREATOR, xmlReaderCreator);
		}
		return mapBuilder.toPropertyMap();
	}

	private static ErrorHandler createErrorHandler(XMLErrorReporter reporter) {
		XMLErrorHandler handler = new RelaxNGErrorHandler(reporter);
		return new ErrorHandlerProxy() {
			protected XMLErrorHandler getErrorHandler() {
				return handler;
			};
		};
	}

	public static void validate(Schema schema, XMLReader xr, XMLErrorReporter errorReporter) {
		PropertyMap instanceProperties = createPropertyMap(null, errorReporter, null, null);
		Validator validator = schema.createValidator(instanceProperties);
		xr.setContentHandler(validator.getContentHandler());
	}

}
