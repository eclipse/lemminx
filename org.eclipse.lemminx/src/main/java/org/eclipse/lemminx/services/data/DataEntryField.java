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
package org.eclipse.lemminx.services.data;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.utils.JSONUtility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Data entry field support used to resolve :
 *
 * <ul>
 *
 * <li>resolve completion :
 * https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#completionItem
 * </li>
 * <li>resolve codeAction :
 * https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#codeAction
 * </li>
 * </ul>
 *
 * Here a data sample for codeAction used by the resolve code action:
 *
 * <code>
 * {
        "title": "Generate 'content.dtd' and bind with xml-model",
        "kind": "quickfix",
   		...
        ],
        "data": {
            "uri": "file:///.../content.xml",
            "participantId": "org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.NoGrammarConstraintsCodeAction#xml-model@dtd",
            "file": "file:///.../content.dtd"
        }
    }
 *
 * <p>
 *
 * The two required data field for any resolve (codeAction, completion, etc) are:
 *
 * <ul>
 * 	<li>"uri" : the URI of the document XML where completion, codeAction, etc must be resolved (when user clicks on this quickfix).</li>
 * 	<li>"participantId" : the participant ID which must be used to process the resolve of the completion, codeAction, etc.</li>
 * <li>extra fields (in the below sample "file" is the file URI of the grammar file which must be generated).</li>
 * </ul>
 * </p>
 * </code>
 *
 * @author Angelo ZERR
 *
 */
public class DataEntryField {

	private static final String DATA_URI_FIELD = "uri";

	private static final String DATA_PARTICIPANT_ID_FIELD = "participantId";

	public static final String OFFSET_FIELD = "offset";

	/**
	 * Create a JSON data entry field with the two required information:
	 *
	 * @param uri           the URI of the document XML where completion,
	 *                      codeAction, etc must be resolved (when user clicks on
	 *                      this quickfix).
	 * @param participantId the participant ID which must be used to process the
	 *                      resolve of the completion, codeAction, etc.
	 *
	 * @return the JSON data entry field.
	 */
	public static JsonObject createData(String uri, String participantId) {
		JsonObject data = new JsonObject();
		data.addProperty(DATA_URI_FIELD, uri);
		data.addProperty(DATA_PARTICIPANT_ID_FIELD, participantId);
		return data;
	}

	/**
	 * Returns the URI of the document XML where completion, codeAction, etc must be
	 * resolved (when user clicks on this quickfix) and null otherwise.
	 *
	 * @param data the data entry field comings from the CompletionItem, CodeAction,
	 *             etc.
	 *
	 * @return the URI of the document XML where completion, codeAction, etc must be
	 *         resolved (when user clicks on this quickfix) and null otherwise.
	 */
	public static String getUri(Object data) {
		return getProperty(data, DATA_URI_FIELD);
	}

	/**
	 * Returns the participant ID which must be used to process the resolve of the
	 * completion, codeAction, etc and null otherwise.
	 *
	 * @param data the data entry field comings from the CompletionItem, CodeAction,
	 *             etc.
	 *
	 * @return the participant ID which must be used to process the resolve of the
	 *         completion, codeAction, etc and null otherwise.
	 */
	public static String getParticipantId(Object data) {
		return getProperty(data, DATA_PARTICIPANT_ID_FIELD);
	}

	/**
	 * Returns the property value of the given <code>fieldName</code> if the given
	 * <code>data</code> and null otherwise.
	 *
	 * @param data      the data entry field comings from the CompletionItem,
	 *                  CodeAction, etc.
	 *
	 * @param fieldName the entry field name (ex : "uri", "participantId").
	 *
	 * @return the property value of the given <code>fieldName</code> if the given
	 *         <code>data</code> and null otherwise.
	 */
	public static String getProperty(Object data, String fieldName) {
		JsonObject json = JSONUtility.toModel(data, JsonObject.class);
		if (json == null) {
			return null;
		}
		JsonElement element = json.get(fieldName);
		return element != null ? element.getAsString() : null;
	}

	/**
	 * Returns the value of the field as an integer or null if the field is empty.
	 *
	 * @param data      the object to read the field of
	 * @param fieldName the field to read
	 * @throws ClassCastException if the field is not an integer
	 * @return the value of the field as an integer or null if the field is empty
	 */
	public static Integer getPropertyAsInt(Object data, String fieldName) {
		try {
			JsonObject json = JSONUtility.toModel(data, JsonObject.class);
			if (json == null) {
				return null;
			}
			JsonElement element = json.get(fieldName);
			return element != null ? element.getAsInt() : null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the value of the field as an boolean or null if the field is empty.
	 *
	 * @param data      the object to read the field of
	 * @param fieldName the field to read
	 * @throws ClassCastException if the field is not an boolean
	 * @return the value of the field as an boolean or null if the field is empty
	 */
	public static Boolean getPropertyAsBoolean(Object data, String fieldName) {
		// FIXME: catch exception return null
		JsonObject json = JSONUtility.toModel(data, JsonObject.class);
		if (json == null) {
			return null;
		}
		JsonElement element = json.get(fieldName);
		return element != null ? element.getAsBoolean() : null;
	}

	/**
	 * Returns the minimal data to resolve a completion item.
	 * 
	 * @param request       the completion request.
	 * @param participantId the participant id to retrieve the resolver.
	 * @return the minimal data to resolve a completion item.
	 */
	public static JsonObject createCompletionData(ICompletionRequest request, String participantId) {
		DOMDocument document = request.getNode().getOwnerDocument();
		JsonObject data = createData(document.getDocumentURI(),
				participantId);
		data.addProperty(OFFSET_FIELD,
				request.getOffset());
		return data;
	}

}
