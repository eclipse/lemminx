/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.commons.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a settings field with its configuration scope, mirroring VS Code's
 * setting scopes. Fields annotated with {@code @Scope(ConfigurationScope.RESOURCE)}
 * are included in scoped {@code workspace/configuration} pulls (with
 * {@code scopeUri}). Fields without this annotation are treated as global.
 *
 * @see ConfigurationScope
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Scope {

	ConfigurationScope value();
}
