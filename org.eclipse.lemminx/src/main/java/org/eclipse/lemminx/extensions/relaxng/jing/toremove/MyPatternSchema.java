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

import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.rng.impl.PatternSchema;

/**
 * This class is a copy of {@link PatternSchema} adapted for LemMinx.
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
 */
public class MyPatternSchema extends PatternSchema {

	private final Pattern start;

	public MyPatternSchema(SchemaPatternBuilder spb, Pattern start, PropertyMap properties) {
		super(spb, start, properties);
		this.start = start;
	}

	public Pattern getStart() {
		return start;
	}

}
