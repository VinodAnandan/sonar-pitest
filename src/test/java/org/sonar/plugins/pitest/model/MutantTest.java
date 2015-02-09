/*
 * Sonar Pitest Plugin
 * Copyright (C) 2015 SonarCommunity
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pitest.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MutantTest {

    private Mutant detectedMutant;
    private Mutant undetectedMutant;

    @Before
    public void setUp() throws Exception {

        detectedMutant = new Mutant(true, MutantStatus.KILLED, "SomeClass.java", "com.foo.bar.SomeClass", "anyMethod",
                "anyMethodDesc", 17,
                Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator"), "", 5,
                "com.foo.bar.SomeClassKillingTest");
        undetectedMutant = new Mutant(false, MutantStatus.NO_COVERAGE, "SomeClass.java", "com.foo.bar.SomeClass",
                "anyMethod", "anyMethodDesc", 8,
                Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator"), "EQUAL_ELSE", 10,
                "com.foo.bar.SomeClassKillingTest");
    }

    @Test
    public void testHashCode() throws Exception {

        assertNotEquals(detectedMutant.hashCode(), undetectedMutant.hashCode());

        assertEquals(detectedMutant.hashCode(), detectedMutant.hashCode());
    }

    @Test
    public void testIsDetected_true() throws Exception {

        assertTrue(detectedMutant.isDetected());
    }

    @Test
    public void testIsDetected_false() throws Exception {

        assertTrue(detectedMutant.isDetected());
    }

    @Test
    public void testGetMutantStatus_killed() throws Exception {

        assertEquals(MutantStatus.KILLED, detectedMutant.getMutantStatus());
    }

    @Test
    public void testGetMutantStatus_noCoverage() throws Exception {

        assertEquals(MutantStatus.NO_COVERAGE, undetectedMutant.getMutantStatus());
    }

    @Test
    public void testGetSourceFile() throws Exception {

        assertEquals("SomeClass.java", detectedMutant.getSourceFile());
    }

    @Test
    public void testGetMutatedClass() throws Exception {

        assertEquals("com.foo.bar.SomeClass", detectedMutant.getMutatedClass());
    }

    @Test
    public void testGetMutatedMethod() throws Exception {

        assertEquals("anyMethod", detectedMutant.getMutatedMethod());
    }

    @Test
    public void testGetMethodDescription() throws Exception {

        assertEquals("anyMethodDesc", detectedMutant.getMethodDescription());
    }

    @Test
    public void testGetLineNumber() throws Exception {

        assertEquals(17, detectedMutant.getLineNumber());
        assertEquals(8, undetectedMutant.getLineNumber());
    }

    @Test
    public void testGetMutator() throws Exception {

        final Mutator mutator = Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator");
        assertNotNull(mutator);
        assertEquals(mutator, detectedMutant.getMutator());
    }

    @Test
    public void testGetMutatorSuffix_nonEmptySuffix() throws Exception {

        assertEquals("", detectedMutant.getMutatorSuffix());

    }

    @Test
    public void testGetMutatorSuffix_emptySuffix() throws Exception {

        assertEquals("EQUAL_ELSE", undetectedMutant.getMutatorSuffix());

    }

    @Test
    public void testGetIndex() throws Exception {

        assertEquals(5, detectedMutant.getIndex());
        assertEquals(10, undetectedMutant.getIndex());
    }

    @Test
    public void testGetKillingTest() throws Exception {

        assertEquals("com.foo.bar.SomeClassKillingTest", detectedMutant.getKillingTest());
    }

    @Test
    public void testEquals_same_true() throws Exception {

        assertTrue(detectedMutant.equals(detectedMutant));
    }

    @Test
    public void testEquals_different_false() throws Exception {

        assertFalse(undetectedMutant.equals(detectedMutant));
    }

    @Test
    public void testToString() throws Exception {

        assertEquals(
                "Mutant [sourceFile=SomeClass.java, mutatedClass=com.foo.bar.SomeClass, mutatedMethod=anyMethod, methodDescription=anyMethodDesc, lineNumber=17, mutantStatus=KILLED, mutator=org.sonar.plugins.pitest.model.Mutator@4cfaa14a, killingTest=com.foo.bar.SomeClassKillingTest]",
                detectedMutant.toString());

    }

    @Test
    public void testGetPathToSourceFile() throws Exception {

        assertEquals("com/foo/bar/SomeClass.java", detectedMutant.getPathToSourceFile());
    }

}
