/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.junit;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayNameGenerator;

public class CamelCaseDisplayNameGenerator extends DisplayNameGenerator.Standard {

    public String generateDisplayNameForClass(Class<?> testClass) {
        return splitCamelCase(super.generateDisplayNameForClass(testClass));
    }

    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
        return splitCamelCase(super.generateDisplayNameForNestedClass(nestedClass));
    }

    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        return splitCamelCase(testMethod.getName());
    }

    String splitCamelCase(String text) {
    	//See https://stackoverflow.com/a/15370765/753170
        return text.replaceAll("([A-Z])([A-Z])([a-z])|([a-z])([A-Z])", "$1$4 $2$3$5");
    }
}