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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// @formatter:off
public class CamelCaseDisplayNameGeneratorTest {

    private CamelCaseDisplayNameGenerator nameGenerator;

    @BeforeEach
    public void setup() {
        nameGenerator = new CamelCaseDisplayNameGenerator();
    }

    @Test
    public void generateDisplayNameForClass() {
        assertEquals("Camel Case Display Name Generator", nameGenerator.generateDisplayNameForClass(nameGenerator.getClass()));
    }

    @ParameterizedTest(name = "{index} => text={0}")
    @CsvSource({ "ABCD, ABCD", 
                 "AbCd, Ab Cd", 
                 "abCd, ab Cd", 
                 "aBCd, a BCd", 
                 "a BC d, a BC d" })
    public void splitCamelCase(String text, String expectedResult) {
        assertEquals(expectedResult, nameGenerator.splitCamelCase(text));
    }
}