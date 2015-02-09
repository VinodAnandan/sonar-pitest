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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MutantBuilderTest {

    private MutantBuilder subject;

    @Before
    public void setUp() throws Exception {

        subject = new MutantBuilder();
    }

    @Test
    public void testDetected_isDetected_true() throws Exception {

        subject.detected(true);
        assertTrue(subject.build().isDetected());
    }

    @Test
    public void testDetected_isNotDetected_false() throws Exception {

        subject.detected(false);
        assertFalse(subject.build().isDetected());
    }

    @Test
    public void testMutantStatusMutantStatus() throws Exception {

        subject.mutantStatus(MutantStatus.KILLED);
        assertEquals(MutantStatus.KILLED, subject.build().getMutantStatus());
    }

    @Test
    public void testMutantStatusString_validStatus() throws Exception {

        subject.mutantStatus("KILLED");
        assertEquals(MutantStatus.KILLED, subject.build().getMutantStatus());
    }

    @Test
    public void testMutantStatusString_invalidStatus_unknown() throws Exception {

        subject.mutantStatus("invalidStatus");
        assertEquals(MutantStatus.UNKNOWN, subject.build().getMutantStatus());
    }

    @Test
    public void testInSourceFile() throws Exception {

        subject.inSourceFile("someSource.java");
        assertEquals("someSource.java", subject.build().getSourceFile());
    }

    @Test
    public void testInClass() throws Exception {

        subject.inClass("some.package.SomeClass");
        assertEquals("some.package.SomeClass", subject.build().getMutatedClass());
    }

    @Test
    public void testInMethod() throws Exception {

        subject.inMethod("aMethod");
        assertEquals("aMethod", subject.build().getMutatedMethod());
    }

    @Test
    public void testWithMethodParameters() throws Exception {

        subject.withMethodParameters("methodDescription");
        assertEquals("methodDescription", subject.build().getMethodDescription());
    }

    @Test
    public void testInLine() throws Exception {

        subject.inLine(123);
        assertEquals(123, subject.build().getLineNumber());
    }

    @Test
    public void testUsingMutatorMutator() throws Exception {

        // prepare
        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        // act
        subject.usingMutator(Mutator.find("ARGUMENT_PROPAGATION"));
        // assert
        assertEquals(mutator, subject.build().getMutator());
    }

    @Test
    public void testUsingMutatorString() throws Exception {

        final Mutator mutator = Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator");
        assertNotNull(mutator);

        subject.usingMutator("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator");
        assertEquals(mutator, subject.build().getMutator());
    }

    @Test
    public void testUsingMutatorString_withSuffix() throws Exception {

        final Mutator mutator = Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator");
        assertNotNull(mutator);

        subject.usingMutator("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_A_SUFFIX");
        assertEquals(mutator, subject.build().getMutator());
        assertEquals("A_SUFFIX", subject.build().getMutatorSuffix());
    }

    @Test
    public void testAtIndex() throws Exception {

        subject.atIndex(123);
        assertEquals(123, subject.build().getIndex());
    }

    @Test
    public void testKilledBy() throws Exception {

        subject.killedBy("killingTest");
        assertEquals("killingTest", subject.build().getKillingTest());
    }

}
