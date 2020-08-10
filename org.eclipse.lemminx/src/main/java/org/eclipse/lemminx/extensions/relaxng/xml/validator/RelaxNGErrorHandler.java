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
package org.eclipse.lemminx.extensions.relaxng.xml.validator;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.eclipse.lemminx.extensions.relaxng.RelaxNGConstants;

/**
 * {@link XMLErrorHandler} Xerces implementation for RelaxNG.
 * 
 * @author Angelo ZERR
 *
 */
public class RelaxNGErrorHandler implements XMLErrorHandler {

	private XMLErrorReporter report;

	public RelaxNGErrorHandler(XMLErrorReporter report) {
		this.report = report;
	}

	private static class RelaxNGReportInfo {

		public final RelaxNGErrorCode errorCode;

		public final Object[] arguments;

		public RelaxNGReportInfo(RelaxNGErrorCode errorCode, Object... arguments) {
			this.errorCode = errorCode;
			this.arguments = arguments;
		}
	}

	@Override
	public void warning(String domain, String key, XMLParseException exception) throws XNIException {
		reportError(domain, key, XMLErrorReporter.SEVERITY_WARNING, exception);
	}

	@Override
	public void fatalError(String domain, String key, XMLParseException exception) throws XNIException {
		reportError(domain, key, XMLErrorReporter.SEVERITY_FATAL_ERROR, exception);
	}

	@Override
	public void error(String domain, String key, XMLParseException exception) throws XNIException {
		reportError(domain, key, XMLErrorReporter.SEVERITY_ERROR, exception);
	}

	private void reportError(String domain, String key, short severity, XMLParseException exception) {
		if (domain.isEmpty() && key.isEmpty()) {
			// RelaxNG
			String message = exception.getMessage();
			if (!message.isEmpty()) {
				RelaxNGReportInfo info = getRelaxNGReportInfo(message);
				report.reportError(RelaxNGConstants.RELAX_NG_DOMAIN, info.errorCode.getCode(), info.arguments, severity,
						exception);
				return;
			}
		}
		report.reportError(domain, key, null, severity, exception);
	}

	private static RelaxNGReportInfo getRelaxNGReportInfo(String message) {
		// Elements
		if (message.startsWith("element")) {
			if (message.contains(" not allowed anywhere")) {
				// unknown_element=element {0} not allowed anywhere{1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.unknown_element);
			}
			if (message.contains("not allowed yet; missing required element ")) {
				// unexpected_element_required_element_missing=element {0} not allowed yet;
				// missing required element {1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.unexpected_element_required_element_missing);
			}
			if (message.contains("incomplete; missing required elements")) {
				// incomplete_element_required_elements_missing=element {0} incomplete; missing
				// required elements {1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.incomplete_element_required_elements_missing);
			}
			if (message.contains("incomplete; missing required element")) {
				// incomplete_element_required_element_missing=element {0} incomplete; missing
				// required element {1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.incomplete_element_required_element_missing);
			}
			if (message.contains("incomplete")) {
				// incomplete_element_required_elements_missing_expected=element {0}
				// incomplete{1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.incomplete_element_required_elements_missing_expected);
			}
			if (message.contains("not allowed here")) {
				// out_of_context_element=element {0} not allowed here{1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.out_of_context_element);
			}

			// Required attributes

			if (message.contains("missing one or more required attributes")) {
				// required_attributes_missing_expected=element {0} missing one or more required
				// attributes{1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.required_attributes_missing_expected);
			}
			if (message.contains("missing required attributes ")) {
				// required_attributes_missing=element {0} missing required attributes {1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.required_attributes_missing);
			}
			if (message.contains("missing required attribute ")) {
				// required_attribute_missing=element {0} missing required attribute {1}
				return new RelaxNGReportInfo(RelaxNGErrorCode.required_attribute_missing);
			}

		}

		// Attributes
		if (message.startsWith("found attribute")) {
			// no_attributes_allowed=found attribute {0}, but no attributes allowed here
			String attrName = extractArg("found attribute", message);
			return new RelaxNGReportInfo(RelaxNGErrorCode.no_attributes_allowed, attrName);
		}
		if (message.startsWith("attribute")) {
			if (message.contains(" not allowed here")) {
				// invalid_attribute_name=attribute {0} not allowed here{1}
				// ex : attribute "bad1" not allowed here...
				String attrName = extractArg("attribute", message);
				return new RelaxNGReportInfo(RelaxNGErrorCode.invalid_attribute_name, attrName);
			}
		}
		if (message.startsWith("value of attribute ")) {
			// invalid_attribute_value=value of attribute {0} is invalid{1}
			// ex : value of attribute "bad1" not allowed here...
			String attrName = extractArg("value of attribute ", message);
			return new RelaxNGReportInfo(RelaxNGErrorCode.invalid_attribute_value, attrName);
		}

		return new RelaxNGReportInfo(RelaxNGErrorCode.to_implement);
	}

	private static String extractArg(String from, String message) {
		int fromIndex = message.indexOf(from) + from.length();
		StringBuilder arg = null;
		for (int i = fromIndex; i < message.length(); i++) {
			char c = message.charAt(i);
			if (c == '"') {
				if (arg == null) {
					arg = new StringBuilder();
				} else {
					break;
				}
			} else {
				if (arg != null) {
					arg.append(c);
				}
			}
		}
		return arg != null ? arg.toString() : null;
	}
}
