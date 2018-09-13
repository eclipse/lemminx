/**
 *  Copyright (c) 2018 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.settings.capabilities;

import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.CODE_ACTION_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.COMPLETION_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_LINK_OPTIONS;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_HIGHLIGHT_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_SYMBOL_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.FOLDING_RANGE_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.HOVER_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.LINK_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.RENAME_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_CODE_ACTION;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_COMPLETION;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DOCUMENT_SYMBOL;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_FOLDING_RANGE;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_HIGHLIGHT;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_HOVER;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_LINK;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_RENAME;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.FORMATTING_ID;
import static org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesConstants.FORMATTING_RANGE_ID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4xml.XMLTextDocumentService;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * Manager for capability related tasks
 */
public class XMLCapabilityManager {

	private ClientCapabilitiesWrapper clientWrapper;
	private Set<String> registeredCapabilities = new HashSet<>(3);
	private LanguageClient languageClient;
	private XMLTextDocumentService textDocumentService;

	public XMLCapabilityManager(LanguageClient languageClient, XMLTextDocumentService textDocumentService) {
		this.languageClient = languageClient;
		this.textDocumentService = textDocumentService;
	}

	/**
	 * Creates and sets a {@link ClientCapabilitiesWrapper} instance formed from
	 * clientCapabilities
	 * 
	 * @param clientCapabilities
	 */
	public void setClientCapabilities(ClientCapabilities clientCapabilities) {
		this.clientWrapper = new ClientCapabilitiesWrapper(clientCapabilities);
	}

	public ClientCapabilitiesWrapper getClientCapabilities() {
		if (this.clientWrapper == null) {
			this.clientWrapper = new ClientCapabilitiesWrapper();
		}
		return this.clientWrapper;
	}

	public void toggleCapability(boolean enabled, String id, String capability, Object options) {
		if (enabled) {
			registerCapability(id, capability, options);
		} else {
			unregisterCapability(id, capability);
		}
	}

	public void unregisterCapability(String id, String method) {
		if (registeredCapabilities.remove(id)) {
			Unregistration unregistration = new Unregistration(id, method);
			UnregistrationParams unregistrationParams = new UnregistrationParams(
					Collections.singletonList(unregistration));
			languageClient.unregisterCapability(unregistrationParams);
		}
	}

	public void registerCapability(String id, String method) {
		registerCapability(id, method, null);
	}

	public void registerCapability(String id, String method, Object options) {
		if (registeredCapabilities.add(id)) {
			Registration registration = new Registration(id, method, options);
			RegistrationParams registrationParams = new RegistrationParams(Collections.singletonList(registration));
			languageClient.registerCapability(registrationParams);
		}
	}

	/**
	 * Registers all dynamic capabilities that the server does not support client
	 * side preferences turning on/off
	 */
	public void initializeCapabilities() {
		if (this.getClientCapabilities().isCodeActionDynamicRegistered()) {
			registerCapability(CODE_ACTION_ID, TEXT_DOCUMENT_CODE_ACTION);
		}
		if (this.getClientCapabilities().isCompletionDynamicRegistrationSupported()) {
			registerCapability(COMPLETION_ID, TEXT_DOCUMENT_COMPLETION, DEFAULT_COMPLETION_OPTIONS);
		}
		if (this.getClientCapabilities().isDocumentHighlightDynamicRegistered()) {
			registerCapability(DOCUMENT_HIGHLIGHT_ID, TEXT_DOCUMENT_HIGHLIGHT);
		}
		if (this.getClientCapabilities().isDocumentSymbolDynamicRegistered()) {
			registerCapability(DOCUMENT_SYMBOL_ID, TEXT_DOCUMENT_DOCUMENT_SYMBOL);
		}
		if (this.getClientCapabilities().isRangeFoldingDynamicRegistrationSupported()) {
			registerCapability(FOLDING_RANGE_ID, TEXT_DOCUMENT_FOLDING_RANGE);
		}
		if (this.getClientCapabilities().isHoverDynamicRegistered()) {
			registerCapability(HOVER_ID, TEXT_DOCUMENT_HOVER);
		}
		if (this.getClientCapabilities().isLinkDynamicRegistrationSupported()) {
			registerCapability(LINK_ID, TEXT_DOCUMENT_LINK, DEFAULT_LINK_OPTIONS);
		}
		if (this.getClientCapabilities().isRenameDynamicRegistrationSupported()) {
			registerCapability(RENAME_ID, TEXT_DOCUMENT_RENAME);
		}

		syncDynamicCapabilitiesWithPreferences();
	}

	/**
	 * Registers all capabilities that this server can support client side
	 * preferences to turn on/off
	 * 
	 * If a capability is not dynamic, it's handled by
	 * {@link ServerCapabilitiesInitializer}
	 */
	public void syncDynamicCapabilitiesWithPreferences() {
		XMLFormattingOptions formattingPreferences = this.textDocumentService.getSharedFormattingOptions();

		if (this.getClientCapabilities().isFormattingDynamicRegistrationSupported()) {
			toggleCapability(formattingPreferences.isEnabled(), FORMATTING_ID,
					ServerCapabilitiesConstants.TEXT_DOCUMENT_FORMATTING, null);
		}

		if (this.getClientCapabilities().isRangeFormattingDynamicRegistrationSupported()) {
			toggleCapability(formattingPreferences.isEnabled(), FORMATTING_RANGE_ID,
					ServerCapabilitiesConstants.TEXT_DOCUMENT_RANGE_FORMATTING, null);
		}
	}

	public Set<String> getRegisteredCapabilities() {
		return this.registeredCapabilities;
	}

}