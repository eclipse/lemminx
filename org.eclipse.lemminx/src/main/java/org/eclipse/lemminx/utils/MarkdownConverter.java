/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lemminx.utils;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeXml;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.overzealous.remark.Options;
import com.overzealous.remark.Options.FencedCodeBlocks;
import com.overzealous.remark.Options.Tables;
import com.overzealous.remark.Remark;

import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.ListBlock;
import org.commonmark.node.Node;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

/**
 * Converts HTML content into Markdown equivalent.
 *
 * @author Fred Bricon
 */
public class MarkdownConverter {

	private static final Logger LOGGER = Logger.getLogger(MarkdownConverter.class.getName());

	private static Remark remark;

	private static Parser commonMarkParser;

	private static HtmlRenderer commonMarkRenderer;

	private MarkdownConverter() {
		// no public instantiation
	}

	static {
		Options options = new Options();
		options.tables = Tables.CONVERT_TO_CODE_BLOCK;
		options.inlineLinks = true;
		options.autoLinks = true;
		options.reverseHtmlSmartPunctuation = true;
		options.fencedCodeBlocks = FencedCodeBlocks.ENABLED_BACKTICK;
		remark = new Remark(options);
		// Stop remark from stripping file protocol in an href
		try {
			Field cleanerField = Remark.class.getDeclaredField("cleaner");
			cleanerField.setAccessible(true);

			Cleaner c = (Cleaner) cleanerField.get(remark);

			Field whitelistField = Cleaner.class.getDeclaredField("whitelist");
			whitelistField.setAccessible(true);

			Whitelist w = (Whitelist) whitelistField.get(c);

			w.addProtocols("a", "href", "file");
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LOGGER.severe("Unable to modify jsoup to include file protocols " + e.getMessage());
		}
		// Setup CommonMark
		Set<Class<? extends Block>> blockConfig = new HashSet<>();
		blockConfig.add(Heading.class);
		blockConfig.add(HtmlBlock.class);
		blockConfig.add(ThematicBreak.class);
		blockConfig.add(FencedCodeBlock.class);
		blockConfig.add(BlockQuote.class);
		blockConfig.add(ListBlock.class);
		commonMarkParser = Parser.builder() //
				.enabledBlockTypes(blockConfig) //
				.build();
		commonMarkRenderer = HtmlRenderer.builder() //
				.build();
	}

	public static String convert(String html) {
		// Cycle from mixed HTML and Markdown -> just HTML -> Markdown without HTML tags
		html = renderMarkdown(unescapeXml(html));
		return unescapeJava(remark.convert(html));
	}

	private static String renderMarkdown(String html) {
		Node document = commonMarkParser.parse(html);
		return commonMarkRenderer.render(document);
	}

}
