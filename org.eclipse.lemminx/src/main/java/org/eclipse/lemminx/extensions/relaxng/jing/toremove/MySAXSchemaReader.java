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
package org.eclipse.lemminx.extensions.relaxng.jing.toremove;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.validate.rng.SAXSchemaReader;

/**
 * This class is a copy of {@link SAXSchemaReader} adapted for LemMinx.
 * This class could be removed once issues
 * 
 * <ul>
 * <li>https://github.com/relaxng/jing-trang/pull/273</li>
 * <li>https://github.com/relaxng/jing-trang/issues/275</li>
 * </ul>
 * 
 * will be fixed.
 * 
 * @author Angelo ZERR
 *
 */
public class MySAXSchemaReader extends MySchemaReaderImpl {
	private static final MySAXSchemaReader theInstance = new MySAXSchemaReader();

	private MySAXSchemaReader() {
	}

	public static MySAXSchemaReader getInstance() {
		return theInstance;
	}

	protected Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> createParseable(
			SAXSource source, SAXResolver resolver, ErrorHandler eh, PropertyMap properties) throws SAXException {
		if (source.getXMLReader() == null)
			source = new SAXSource(resolver.createXMLReader(), source.getInputSource());
		return new SAXParseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl>(source,
				resolver, eh);
	}
}
