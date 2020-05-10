/*******************************************************************************
* Copyright (c) 2020 Balduin Landolt.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Balduin Landolt <balduin.landolt@hotmail.com> - initial API and implementation
*******************************************************************************/

package org.eclipse.lemminx.dom;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.ProcessingInstruction;

/**
 * XMLModel
 * 
 * xml-model processing instruction {@code <?xml-model ...>}
 * 
 * see https://www.w3.org/TR/xml-model/
 */
public class XMLModel {

	
	// possible attributes according to https://www.w3.org/TR/xml-model/
	// but there may be more.
	// LATER: what to do with things like e.g.: <?oxygen RNGSchema="http://..." type="xml" ?>
	public static final String HREF = "href";
	public static final String TYPE = "type";
	public static final String SCHEMATYPENS = "schematypens";
	public static final String CHARSET = "charset";
	public static final String TITLE = "title";
	public static final String GROUP = "group";
	public static final String PHASE = "phase";

	private Map<String, String> valuePairs;

	/**
	 * Creates an XMLModel object from a ProcessingInstruction object.
	 * It is assumed that the processing instruction is of type {@code <?xml-model ?>}
	 * 
	 * @param pi xml-model Processing Instruction
	 */
	public XMLModel(ProcessingInstruction pi) {
		valuePairs = new HashMap<>();
		String data = pi.getData();
		String[] tokens = data.split("\\s");
		for (String token : tokens) {
			putAttribute(token);
		}
	}

	private void putAttribute(String token) {
		String[] ss = token.split("=");
		if (ss.length < 2)
			return;
		String key = ss[0].toLowerCase().trim();
		String val = ss[1].replace("\"", "");
		valuePairs.put(key, val);
	}

	public String getAttribute(String name){
		return valuePairs.get(name);
	}

	public String getSchemaLocation(){
		return getAttribute(HREF);
	}

	// TODO: add methods for other typicle attributes (see fields)

}

