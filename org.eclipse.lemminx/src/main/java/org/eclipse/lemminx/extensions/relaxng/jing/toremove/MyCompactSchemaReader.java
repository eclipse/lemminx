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

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.validate.rng.CompactSchemaReader;

/**
 * This class is a copy of {@link CompactSchemaReader} adapted for
 * LemMinx. This class could be removed once issues
 * 
 * <ul>
 * <li>https://github.com/relaxng/jing-trang/pull/273</li>
 * <li>https://github.com/relaxng/jing-trang/issues/275</li>
 * </ul>
 * 
 * will be fixed.
 * 
 * @author Angelo ZERR
 */
public class MyCompactSchemaReader extends MySchemaReaderImpl {
	private static final MyCompactSchemaReader theInstance = new MyCompactSchemaReader();

	private MyCompactSchemaReader() {
	}

	public static MyCompactSchemaReader getInstance() {
		return theInstance;
	}

	protected Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> createParseable(
			SAXSource source, SAXResolver saxResolver, ErrorHandler eh, PropertyMap properties) {
		return new CompactParseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl>(
				SAX.createInput(source.getInputSource()), saxResolver.getResolver(), eh);
	}
}
