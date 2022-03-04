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
package org.eclipse.lemminx.extensions.xerces;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.xs.opti.SchemaParsingConfig;

/**
 * The Xerces {@link SchemaParsingConfig} doesn't give the capability to
 * customize the XML entity manager.
 * 
 * <p>
 * This class extends {@link SchemaParsingConfig} and customizes the XML entity
 * manager with the {@link LSPXMLEntityManager} which handles the remote
 * resource download errors.
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class LSPSchemaParsingConfig extends SchemaParsingConfig {

	public LSPSchemaParsingConfig(XMLEntityManager entityManager) {
		super();
		// Replace the fEntityManager created by the SchemaParsingConfig constructor
		// with the custom entityManager.
		if (entityManager != null) {
			fProperties.put(ENTITY_MANAGER, entityManager);
			addComponent(entityManager);
		}
	}
}
