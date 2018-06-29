/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.xml.languageserver.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

import org.eclipse.xml.languageserver.extensions.ICompletionParticipant;

/**
 * XML extensions registry.
 *
 */
public class XMLExtensionsRegistry {

	private Collection<ICompletionParticipant> completionParticipants;

	public Collection<ICompletionParticipant> getCompletionParticipants() {
		if (completionParticipants == null) {
			completionParticipants = loadCompletionParticipants();
		}
		return completionParticipants;
	}

	/**
	 * Load {@link ICompletionParticipant} with SPI.
	 * 
	 * @return the loaded {@link ICompletionParticipant} with SPI.
	 */
	private synchronized Collection<ICompletionParticipant> loadCompletionParticipants() {
		if (completionParticipants != null) {
			return completionParticipants;
		}
		Collection<ICompletionParticipant> completionParticipants = new ArrayList<ICompletionParticipant>();
		ServiceLoader<ICompletionParticipant> loader = ServiceLoader.load(ICompletionParticipant.class);
		loader.forEach(p -> completionParticipants.add(p));
		return completionParticipants;
	}

	/**
	 * Add the given completion participant.
	 * 
	 * @param completionParticipant
	 */
	public void addCompletionParticipant(ICompletionParticipant completionParticipant) {
		completionParticipants.add(completionParticipant);
	}

	/**
	 * Remove the given completion participant.
	 * 
	 * @param completionParticipant
	 */
	public void removeCompletionParticipant(ICompletionParticipant completionParticipant) {
		completionParticipants.remove(completionParticipant);
	}

}
