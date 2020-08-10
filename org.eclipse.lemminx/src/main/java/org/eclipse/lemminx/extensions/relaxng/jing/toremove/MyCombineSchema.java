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

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.CombineSchema;
import com.thaiopensource.validate.Schema;

/**
 * This class is a copy of {@link CombineSchema} adapted for LemMinx. This
 * class could be removed once issues
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
public class MyCombineSchema extends CombineSchema {

	private final Schema schema1;
	private final Schema schema2;

	public MyCombineSchema(Schema schema1, Schema schema2, PropertyMap properties) {
		super(schema1, schema2, properties);
		this.schema1 = schema1;
		this.schema2 = schema2;
	}

	public Schema getSchema1() {
		return schema1;
	}

	public Schema getSchema2() {
		return schema2;
	}
}
