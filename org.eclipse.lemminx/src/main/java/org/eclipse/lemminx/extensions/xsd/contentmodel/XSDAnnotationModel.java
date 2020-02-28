/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.xsd.contentmodel;

import static org.eclipse.lemminx.dom.parser.Constants.DOCUMENTATION_CONTENT;
import static org.eclipse.lemminx.utils.StringUtils.isEmpty;
import static org.eclipse.lemminx.utils.StringUtils.normalizeSpace;

import java.io.StringReader;
import java.util.regex.Matcher;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Extract xs:document & xs:appinfo from the xs:annotation.
 *
 */
class XSDAnnotationModel {

	String appInfo;

	String documentation;

	private XSDAnnotationModel() {

	}

	public String getAppInfo() {
		return appInfo;
	}

	public String getDocumentation() {
		return documentation;
	}

	public static String getDocumentation(XSObjectList annotations) {
		return getDocumentation(annotations, null);
	}

	public static String getDocumentation(XSObjectList annotations, String value) {
		if (annotations == null) {
			return "";
		}
		StringBuilder doc = new StringBuilder();
		for (Object object : annotations) {
			XSAnnotation annotation = null;
			if (object instanceof XSMultiValueFacet && value != null) {
				XSMultiValueFacet multiValueFacet = (XSMultiValueFacet) object;
				ObjectList enumerationValues = multiValueFacet.getEnumerationValues();
				XSObjectList annotationValues = multiValueFacet.getAnnotations();
				for (int i = 0; i < enumerationValues.getLength(); i++) {
					Object enumValue = enumerationValues.get(i);

					// Assuming always ValidatedInfo
					String enumString = ((ValidatedInfo) enumValue).stringValue();

					if (value.equals(enumString)) {
						annotation = (XSAnnotation) annotationValues.get(i);
						break;
					}
				}
			} else if (object instanceof XSAnnotation) {
				annotation = (XSAnnotation) object;
			}

			XSDAnnotationModel annotationModel = XSDAnnotationModel.load(annotation);
			if (annotationModel != null) {
				if (annotationModel.getAppInfo() != null) {
					doc.append(annotationModel.getAppInfo());
				}
				if (annotationModel.getDocumentation() != null) {
					doc.append(annotationModel.getDocumentation());
				} else {
					String annotationString = annotation.getAnnotationString();
					if (!isEmpty(annotationString)) {
						doc.append(getDocumentation(annotationString));
					}
				}
			}
		}
		return doc.toString();
	}

	public static XSDAnnotationModel load(XSAnnotation annotation) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XSAnnotationHandler handler = new XSAnnotationHandler();
			saxParser.parse(new InputSource(new StringReader(annotation.getAnnotationString())), handler);
			return handler.getModel();
		} catch (Exception e) {
			return null;
		}
	}

	private static class XSAnnotationHandler extends DefaultHandler {

		private static final String APPINFO_ELEMENT = "appinfo";
		private static final String DOCUMENTATION_ELEMENT = "documentation";

		private StringBuilder current;
		private final XSDAnnotationModel model;

		public XSAnnotationHandler() {
			model = new XSDAnnotationModel();
		}

		public XSDAnnotationModel getModel() {
			return model;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (qName.endsWith(DOCUMENTATION_ELEMENT) || qName.endsWith(APPINFO_ELEMENT)) {
				current = new StringBuilder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (current != null) {
				if (qName.endsWith(APPINFO_ELEMENT)) {
					model.appInfo = normalizeSpace(current.toString());
				} else if (qName.endsWith(DOCUMENTATION_ELEMENT)) {
					model.documentation = normalizeSpace(current.toString());
				}
				current = null;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (current != null) {
				current.append(ch, start, length);
			}
			super.characters(ch, start, length);
		}

	}

	public static String getDocumentation(String xml) {
		Matcher m = DOCUMENTATION_CONTENT.matcher(xml);
		if(m.find()) {
			return m.group(1);
		}
		return null;
	}

}
