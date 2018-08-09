package org.eclipse.lsp4xml.emmet.utils;

import static org.eclipse.lsp4xml.emmet.emmetio.expand.ExpandAbbreviation.expand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.emmet.emmetio.expand.ExpandOptions;
import org.eclipse.lsp4xml.emmet.emmetio.extract.ExtractAbbreviationResult;
import org.eclipse.lsp4xml.emmet.emmetio.extract.EmmetOptions;
import org.eclipse.lsp4xml.emmet.emmetio.extract.ExtractAbbreviation;

public class EmmetHelper {

	private static final int maxFilters = 3;

	private static final Pattern WORD_PATTERN = Pattern.compile("[\\w,:,-]*$");

	private static class ExtractedValue {

		private final Range abbreviationRange;

		private final String abbreviation;

		private final String filter;

		public ExtractedValue(Range abbreviationRange, String abbreviation, String filter) {
			super();
			this.abbreviationRange = abbreviationRange;
			this.abbreviation = abbreviation;
			this.filter = filter;
		}

		public Range getAbbreviationRange() {
			return abbreviationRange;
		}

		public String getAbbreviation() {
			return abbreviation;
		}

		public String getFilter() {
			return filter;
		}

	}

	public static CompletionList doComplete(TextDocument document, Position position, String syntax,
			EmmetConfiguration emmetConfig) {
		try {
			ExtractedValue extractedValue = extractAbbreviation(document, position,
					new EmmetOptions().setSyntax(syntax).setLookAhead(true));
			if (extractedValue == null) {
				return null;
			}

			ExpandOptions expandOptions = null;

			Range abbreviationRange = extractedValue.getAbbreviationRange();
			String abbreviation = extractedValue.getAbbreviation();
			String filter = extractedValue.getFilter();
			String currentLineTillPosition = getCurrentLine(document, position).substring(0, position.getCharacter());
			String currentWord = getCurrentWord(currentLineTillPosition);

			// Dont attempt to expand open tags
			if (abbreviation.equals(currentWord) && currentLineTillPosition.endsWith("<" + abbreviation))
			// && (syntax === 'html' || syntax === 'xml' || syntax === 'xsl' || syntax ===
			// 'jsx')) {
			{
				return null;
			}

			CompletionList completionItems = new CompletionList();
			// If abbreviation is valid, then expand it and ensure the expanded value is not
			// noise
			if (isAbbreviationValid(syntax, abbreviation)) {
				CompletionItem item = createExpandedAbbr(abbreviation, abbreviationRange, abbreviation, syntax, filter,
						expandOptions);
				if (item != null) {
					completionItems.getItems().add(item);
				}
			}
			return completionItems;

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// Create completion item for expanded abbreviation
	private static CompletionItem createExpandedAbbr(String abbr, Range abbreviationRange, String abbreviation,
			String syntax, String filter, ExpandOptions expandOptions) {
		String expandedText = null;
		try {
			expandedText = expand(abbr, expandOptions);
		} catch (Exception e) {
		}

		if (expandedText != null && isExpandedTextNoise(syntax, abbr, expandedText)) {
			expandedText = "";
		}

		if (expandedText != null) {
			CompletionItem expandedAbbr = new CompletionItem(abbr);
			expandedAbbr.setTextEdit(
					new TextEdit(abbreviationRange, escapeNonTabStopDollar(addFinalTabStop(expandedText))));
			expandedAbbr.setDocumentation(replaceTabStopsWithCursors(expandedText));
			expandedAbbr.setInsertTextFormat(InsertTextFormat.Snippet);
			expandedAbbr.setDetail("Emmet Abbreviation");
			expandedAbbr.setLabel(abbreviation + (filter != null ? "|" + filter.replaceAll(",", "|") : ""));
			return expandedAbbr;
		}
		return null;
	}

	/**
	 * * Extracts abbreviation from the given position in the given document
	 * 
	 * @param document The TextDocument from which abbreviation needs to be
	 *                 extracted
	 * @param position The Position in the given document from where abbreviation
	 *                 needs to be extracted
	 * @param options  The options to pass to the @emmetio/extract-abbreviation
	 *                 module
	 * @throws BadLocationException
	 */
	private static ExtractedValue extractAbbreviation(TextDocument document, Position position, EmmetOptions options)
			throws BadLocationException {
		String currentLine = getCurrentLine(document, position);
		String currentLineTillPosition = currentLine.substring(0, position.getCharacter());
		FilterResult filterResult = getFilters(currentLineTillPosition, position.getCharacter());
		int pos = filterResult.getPos();
		String filter = filterResult.getFilter();
		int lengthOccupiedByFilter = filter != null ? filter.length() + 1 : 0;
		try {
			EmmetOptions extractOptions = options;

			ExtractAbbreviationResult result = extract(currentLine, pos, extractOptions);
			Range rangeToReplace = new Range(new Position(position.getLine(), result.getLocation()),
					new Position(position.getLine(),
							result.getLocation() + result.getAbbreviation().length() + lengthOccupiedByFilter));
			return new ExtractedValue(rangeToReplace, result.getAbbreviation(), filter);
		} catch (Exception e) {
			return null;
		}
	}

	private static ExtractAbbreviationResult extract(String currentLine, int pos, EmmetOptions extractOptions) {
		return ExtractAbbreviation.extractAbbreviation(currentLine, pos, extractOptions);
	}

	private static class FilterResult {

		private final int pos;
		private final String filter;

		public FilterResult(int pos, String filter) {
			super();
			this.pos = pos;
			this.filter = filter;
		}

		public int getPos() {
			return pos;
		}

		public String getFilter() {
			return filter;
		}

	}

	private static FilterResult getFilters(String text, int pos) {
		String filter = null;
		for (int i = 0; i < maxFilters; i++) {
			/*
			 * TODO: AZ if (text.endsWith(`${filterDelimitor}${bemFilterSuffix}`, pos)) {
			 * pos -= bemFilterSuffix.length + 1; filter = filter ? bemFilterSuffix + ',' +
			 * filter : bemFilterSuffix; } else if
			 * (text.endsWith(`${filterDelimitor}${commentFilterSuffix}`, pos)) { pos -=
			 * commentFilterSuffix.length + 1; filter = filter ? commentFilterSuffix + ',' +
			 * filter : commentFilterSuffix; } else if
			 * (text.endsWith(`${filterDelimitor}${trimFilterSuffix}`, pos)) { pos -=
			 * trimFilterSuffix.length + 1; filter = filter ? trimFilterSuffix + ',' +
			 * filter : trimFilterSuffix; } else { break; }
			 */
		}
		return new FilterResult(pos, filter);
	}

	private static String replaceTabStopsWithCursors(String expandedWord) {
		return expandedWord;
		// expandedWord.replace(/([^\\])\$\{\d+\}/g,
		// '$1|').replace(/\$\{\d+:([^\}]+)\}/g,'$1');

	}

	private static String removeTabStops(String expandedWord) {
		return expandedWord;
		// return expandedWord.replace(/([^\\])\$\{\d+\}/g,
		// '$1').replace(/\$\{\d+:([^\}]+)\}/g,'$1');

	}

	private static String escapeNonTabStopDollar(String text) {
		return text;
		// return text != null ? text.replace(/([^\\])(\$)([^\{])/g, '$1\\$2$3') : text;
	}

	private static String addFinalTabStop(String text) {
		if (text == null || text.trim().isEmpty()) {
			return text;
		}

		/*
		 * int maxTabStop = -1; let maxTabStopRanges = []; let foundLastStop = false;
		 * let replaceWithLastStop = false; let i = 0; let n = text.length;
		 * 
		 * try { while (i < n && !foundLastStop) { // Look for ${ if (text[i++] != '$'
		 * || text[i++] != '{') { continue; }
		 * 
		 * // Find tabstop let numberStart = -1; let numberEnd = -1; while (i < n &&
		 * /\d/.test(text[i])) { numberStart = numberStart < 0 ? i : numberStart;
		 * numberEnd = i + 1; i++; }
		 * 
		 * // If ${ was not followed by a number and either } or :, then its not a
		 * tabstop if (numberStart === -1 || numberEnd === -1 || i >= n || (text[i] !=
		 * '}' && text[i] != ':')) { continue; }
		 * 
		 * // If ${0} was found, then break const currentTabStop =
		 * text.substring(numberStart, numberEnd); foundLastStop = currentTabStop ===
		 * '0'; if (foundLastStop) { break; }
		 * 
		 * let foundPlaceholder = false; if (text[i++] == ':') { // TODO: Nested
		 * placeholders may break here while (i < n) { if (text[i] == '}') {
		 * foundPlaceholder = true; break; } i++; } }
		 * 
		 * // Decide to replace currentTabStop with ${0} only if its the max among all
		 * tabstops and is not a placeholder if (Number(currentTabStop) >
		 * Number(maxTabStop)) { maxTabStop = currentTabStop; maxTabStopRanges = [{
		 * numberStart, numberEnd }]; replaceWithLastStop = !foundPlaceholder; } else if
		 * (currentTabStop == maxTabStop) { maxTabStopRanges.push({ numberStart,
		 * numberEnd }); } } } catch (Exception e) {
		 * 
		 * }
		 * 
		 * if (replaceWithLastStop && !foundLastStop) { for (let i = 0; i <
		 * maxTabStopRanges.length; i++) { let rangeStart =
		 * maxTabStopRanges[i].numberStart; let rangeEnd =
		 * maxTabStopRanges[i].numberEnd; text = text.substr(0, rangeStart) + '0' +
		 * text.substr(rangeEnd); } }
		 */

		return text;
	}

	private static String getCurrentLine(TextDocument document, Position position) throws BadLocationException {
		int offset = document.offsetAt(position);
		String text = document.getText();
		int start = 0;
		int end = text.length();
		for (int i = offset - 1; i >= 0; i--) {
			if (text.charAt(i) == '\n') {
				start = i + 1;
				break;
			}
		}
		for (int i = offset; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				end = i;
				break;
			}
		}
		return text.substring(start, end);
	}

	private static String getCurrentWord(String currentLineTillPosition) {
		if (currentLineTillPosition != null) {
			Matcher matches = WORD_PATTERN.matcher(currentLineTillPosition);
			/*
			 * let matches = currentLineTillPosition.match(/[\w,:,-]*$/); if (matches) {
			 * return matches[0]; }
			 */
		}
		return null;
	}

	/**
	 * Returns a boolean denoting validity of given abbreviation in the context of
	 * given syntax Not needed once https://github.com/emmetio/atom-plugin/issues/22
	 * is fixed
	 * 
	 * @param syntax       string
	 * @param abbreviation string
	 */
	private static boolean isAbbreviationValid(String syntax, String abbreviation) {
		if (abbreviation == null) {
			return false;
		}
		/*
		 * if (abbreviation.startsWith("!")) { return !/[^!]/.test(abbreviation); } //
		 * Its common for users to type (sometextinsidebrackets), this should not be
		 * treated as an abbreviation // Grouping in abbreviation is valid only if
		 * preceeded/succeeded with one of the symbols for nesting, sibling, repeater or
		 * climb up if (!/\(.*\)[>\+\*\^]/.test(abbreviation) &&
		 * !/[>\+\*\^]\(.*\)/.test(abbreviation) && /\(/.test(abbreviation) &&
		 * /\)/.test(abbreviation)) { return false; }
		 * 
		 * return (htmlAbbreviationStartRegex.test(abbreviation) &&
		 * htmlAbbreviationRegex.test(abbreviation));
		 */
		return true;
	}

	private static boolean isExpandedTextNoise(String syntax, String abbreviation, String expandedText) {
		/*
		 * if (commonlyUsedTags.indexOf(abbreviation.toLowerCase()) > -1 ||
		 * markupSnippetKeys.indexOf(abbreviation) > -1) { return false; }
		 * 
		 * // Custom tags can have - or : if (/[-,:]/.test(abbreviation) &&
		 * !/--|::/.test(abbreviation) && !abbreviation.endsWith(':')) { return false; }
		 * 
		 * // Its common for users to type some text and end it with period, this should
		 * not be treated as an abbreviation // Else it becomes noise. if
		 * (/^[a-z,A-Z,\d]*\.$/.test(abbreviation)) { return true; }
		 * 
		 * // Unresolved html abbreviations get expanded as if it were a tag // Eg: abc
		 * -> <abc></abc> which is noise if it gets suggested for every word typed
		 * return (expandedText.toLowerCase() ===
		 * `<${abbreviation.toLowerCase()}>\${1}</${abbreviation.toLowerCase()}>`);
		 */
		return false;
	}
}
