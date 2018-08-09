package org.eclipse.lsp4xml.emmet.emmetio.expand;

import java.io.InputStreamReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @see https://github.com/emmetio/expand-abbreviation/blob/master/index.js
 *
 */
public class ExpandAbbreviation {

	private static ScriptEngine engine;

	static {
		// load extract-abbreviation-full.js
		try {
			engine = new ScriptEngineManager().getEngineByName("nashorn");
			// https://cdnjs.cloudflare.com/ajax/libs/babel-polyfill/6.26.0/polyfill.min.js
			engine.eval(new InputStreamReader(ExpandAbbreviation.class.getResourceAsStream("polyfill.min.js")));
			// generated from expand-abbreviation github project with "no modules"
			engine.eval(new InputStreamReader(ExpandAbbreviation.class.getResourceAsStream("expand-full.js")));
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public static String expand(String abbr, ExpandOptions config) {
		try {
			Invocable inv = (Invocable) engine;
			StringBuilder options = new StringBuilder("({");
			// disable html snippets (to avoid generates <a href="" ></a> whith a abbr
			options.append("snippets: emmet.createSnippetsRegistry(null, 'xml', {})");
			//options.append(", syntax: \"" + config.getSyntax() + "\"");
			options.append("})");
			Object jsOptions = engine.eval(options.toString());
			Object obj = engine.get("emmet");
			return (String) inv.invokeMethod(obj, "expand", abbr, jsOptions);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
