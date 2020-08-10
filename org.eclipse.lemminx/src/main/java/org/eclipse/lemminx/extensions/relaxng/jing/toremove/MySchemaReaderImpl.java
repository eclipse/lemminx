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

import java.io.IOException;

import javax.xml.transform.sax.SAXSource;

import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.FeasibleTransform;
import com.thaiopensource.relaxng.pattern.IdTypeMap;
import com.thaiopensource.relaxng.pattern.IdTypeMapBuilder;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.PatternDumper;
import com.thaiopensource.relaxng.pattern.SchemaBuilderImpl;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.AbstractSchemaReader;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.prop.wrap.WrapProperty;
import com.thaiopensource.validate.rng.impl.FeasibleIdTypeMapSchema;
import com.thaiopensource.validate.rng.impl.IdTypeMapSchema;
import com.thaiopensource.validate.rng.impl.SchemaReaderImpl;

/**
 * This class is a copy of {@link SchemaReaderImpl} adapted for LemMinx.
 * This class could be removed once issues
 * 
 * <ul>
 * <li>https://github.com/relaxng/jing-trang/pull/273</li>
 * <li>https://github.com/relaxng/jing-trang/issues/275</li>
 * </ul>
 * 
 * will be fixed.
 * 
 * See comment with ' UPDATED by LemMinx'
 * 
 * @author Angelo ZERR
 *
 */
public abstract class MySchemaReaderImpl extends AbstractSchemaReader {

	public static final PropertyId<SchemaPatternBuilder> SCHEMA_PATTERN_BUILDER = PropertyId
			.newInstance("SCHEMA_PATTERN_BUILDER", SchemaPatternBuilder.class);

	private static final PropertyId<?>[] supportedPropertyIds = {
			ValidateProperty.XML_READER_CREATOR,
			ValidateProperty.ERROR_HANDLER,
			ValidateProperty.ENTITY_RESOLVER,
			ValidateProperty.URI_RESOLVER,
			ValidateProperty.RESOLVER,
			RngProperty.DATATYPE_LIBRARY_FACTORY,
			RngProperty.CHECK_ID_IDREF,
			RngProperty.FEASIBLE,
			WrapProperty.ATTRIBUTE_OWNER,
			SCHEMA_PATTERN_BUILDER
	};

	public Schema createSchema(SAXSource source, PropertyMap properties)
			throws IOException, SAXException, IncorrectSchemaException {
		// UPDATED by LemMinx
		SchemaPatternBuilder spb = properties.get(SCHEMA_PATTERN_BUILDER);
		if (spb == null) {
			spb = new SchemaPatternBuilder();
		}
		// SchemaPatternBuilder spb = new SchemaPatternBuilder();
		SAXResolver resolver = ResolverFactory.createResolver(properties);
		ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
		DatatypeLibraryFactory dlf = properties.get(RngProperty.DATATYPE_LIBRARY_FACTORY);
		if (dlf == null)
			dlf = new DatatypeLibraryLoader();
		try {
			Pattern start = SchemaBuilderImpl.parse(createParseable(source, resolver, eh, properties), eh, dlf, spb,
					properties.contains(WrapProperty.ATTRIBUTE_OWNER));
			return wrapPattern(start, spb, properties);
		} catch (IllegalSchemaException e) {
			throw new IncorrectSchemaException();
		}
	}

	public Option getOption(String uri) {
		return RngProperty.getOption(uri);
	}

	static private class SimplifiedSchemaPropertyMap implements PropertyMap {
		private final PropertyMap base;
		private final Pattern start;

		SimplifiedSchemaPropertyMap(PropertyMap base, Pattern start) {
			this.base = base;
			this.start = start;
		}

		public <T> T get(PropertyId<T> pid) {
			if (pid == RngProperty.SIMPLIFIED_SCHEMA) {
				String simplifiedSchema = PatternDumper.toString(start);
				return pid.getValueClass().cast(simplifiedSchema);
			} else
				return base.get(pid);
		}

		public PropertyId<?> getKey(int i) {
			return i == base.size() ? RngProperty.SIMPLIFIED_SCHEMA : base.getKey(i);
		}

		public int size() {
			return base.size() + 1;
		}

		public boolean contains(PropertyId<?> pid) {
			return base.contains(pid) || pid == RngProperty.SIMPLIFIED_SCHEMA;
		}
	}

	static Schema wrapPattern(Pattern start, SchemaPatternBuilder spb, PropertyMap properties)
			throws SAXException, IncorrectSchemaException {
		if (properties.contains(RngProperty.FEASIBLE))
			start = FeasibleTransform.transform(spb, start);
		properties = new SimplifiedSchemaPropertyMap(AbstractSchema.filterProperties(properties, supportedPropertyIds),
				start);
		// UPDATED by LemMinx
		// Schema schema = new PatternSchema(spb, start, properties);
		Schema schema = new MyPatternSchema(spb, start, properties);
		if (spb.hasIdTypes() && properties.contains(RngProperty.CHECK_ID_IDREF)) {
			ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
			IdTypeMap idTypeMap = new IdTypeMapBuilder(eh, start).getIdTypeMap();
			if (idTypeMap == null)
				throw new IncorrectSchemaException();
			Schema idSchema;
			if (properties.contains(RngProperty.FEASIBLE))
				idSchema = new FeasibleIdTypeMapSchema(idTypeMap, properties);
			else
				idSchema = new IdTypeMapSchema(idTypeMap, properties);
			// UPDATED by LemMinx
			// schema = new CombineSchema(schema, idSchema, properties);
			schema = new MyCombineSchema(schema, idSchema, properties);
		}
		return schema;
	}

	protected abstract Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> createParseable(
			SAXSource source, SAXResolver resolver, ErrorHandler eh, PropertyMap properties)
			throws SAXException;

}
