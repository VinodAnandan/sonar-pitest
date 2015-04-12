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
    public void testBuild_withMinimalArguments() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                               .mutantStatus(MutantStatus.KILLED)
                               .inSourceFile("someSource.java")
                               .inClass("some.package.SomeClass")
                               .inMethod("aMethod")
                               .withMethodParameters("methodDescription")
                               .usingMutator(Mutator.find("ARGUMENT_PROPAGATION"))
                               .build();
        // @formatter:on
        assertNotNull(mutant);
        assertEquals(MutantStatus.KILLED, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        // implicit default values
        assertEquals(0, mutant.getLineNumber());
        assertEquals(0, mutant.getIndex());
        assertFalse(mutant.isDetected());
        assertEquals("", mutant.getKillingTest());

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("", mutant.getMutatorSuffix());

    }

    @Test
    public void testBuild_detectedWithMinimalArguments() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                                .detected(true)
                                .mutantStatus(MutantStatus.KILLED)
                                .inSourceFile("someSource.java")
                                .inClass("some.package.SomeClass")
                                .inMethod("aMethod")
                                .withMethodParameters("methodDescription")
                                .usingMutator(Mutator.find("ARGUMENT_PROPAGATION"))
                                .build();
        // @formatter:on
        assertNotNull(mutant);
        assertTrue(mutant.isDetected());
        assertEquals(MutantStatus.KILLED, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        // implicit default values
        assertEquals(0, mutant.getLineNumber());
        assertEquals(0, mutant.getIndex());
        assertEquals("", mutant.getKillingTest());

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("", mutant.getMutatorSuffix());

    }

    @Test
    public void testBuild_StatusAsString_WithMinimalArguments() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                                .detected(false)
                                .mutantStatus("KILLED")
                                .inSourceFile("someSource.java")
                                .inClass("some.package.SomeClass")
                                .inMethod("aMethod")
                                .withMethodParameters("methodDescription")
                                .usingMutator(Mutator.find("ARGUMENT_PROPAGATION"))
                                .build();
        // @formatter:on
        assertNotNull(mutant);
        assertFalse(mutant.isDetected());
        assertEquals(MutantStatus.KILLED, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        // implicit default values
        assertEquals(0, mutant.getLineNumber());
        assertEquals(0, mutant.getIndex());
        assertEquals("", mutant.getKillingTest());

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("", mutant.getMutatorSuffix());

    }

    @Test
    public void testBuild_invalidStatus_WithMinimalArguments() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                                .detected(false)
                                .mutantStatus("invalid")
                                .inSourceFile("someSource.java")
                                .inClass("some.package.SomeClass")
                                .inMethod("aMethod")
                                .withMethodParameters("methodDescription")
                                .usingMutator(Mutator.find("ARGUMENT_PROPAGATION"))
                                .build();
        // @formatter:on
        assertNotNull(mutant);
        assertFalse(mutant.isDetected());
        assertEquals(MutantStatus.UNKNOWN, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        // implicit default values
        assertEquals(0, mutant.getLineNumber());
        assertEquals(0, mutant.getIndex());
        assertEquals("", mutant.getKillingTest());

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("", mutant.getMutatorSuffix());

    }

    @Test
    public void testBuild_withLineAndIndexAndMinimalArguments() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                               .mutantStatus(MutantStatus.KILLED)
                               .inSourceFile("someSource.java")
                               .inClass("some.package.SomeClass")
                               .inMethod("aMethod")
                               .inLine(123)
                               .atIndex(456)
                               .withMethodParameters("methodDescription")
                               .usingMutator(Mutator.find("ARGUMENT_PROPAGATION"))
                               .build();
        // @formatter:on
        assertNotNull(mutant);
        assertEquals(MutantStatus.KILLED, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        // implicit default values
        assertEquals(123, mutant.getLineNumber());
        assertEquals(456, mutant.getIndex());
        assertFalse(mutant.isDetected());
        assertEquals("", mutant.getKillingTest());

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("", mutant.getMutatorSuffix());

    }

    @Test
    public void testBuild_withMinimalArguments_MutatorAsStringId() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                               .mutantStatus(MutantStatus.KILLED)
                               .inSourceFile("someSource.java")
                               .inClass("some.package.SomeClass")
                               .inMethod("aMethod")
                               .withMethodParameters("methodDescription")
                               .usingMutator("ARGUMENT_PROPAGATION")
                               .build();
        // @formatter:on
        assertNotNull(mutant);
        assertEquals(MutantStatus.KILLED, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        // implicit default values
        assertEquals(0, mutant.getLineNumber());
        assertEquals(0, mutant.getIndex());
        assertFalse(mutant.isDetected());
        assertEquals("", mutant.getKillingTest());

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("", mutant.getMutatorSuffix());

    }

    @Test
    public void testBuild_MutatorAsStringWithSuffix_withMinimalArguments() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                               .mutantStatus(MutantStatus.KILLED)
                               .inSourceFile("someSource.java")
                               .inClass("some.package.SomeClass")
                               .inMethod("aMethod")
                               .withMethodParameters("methodDescription")
                               .usingMutator("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_A_SUFFIX")
                               .build();
        // @formatter:on
        assertNotNull(mutant);
        assertEquals(MutantStatus.KILLED, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        // implicit default values
        assertEquals(0, mutant.getLineNumber());
        assertEquals(0, mutant.getIndex());
        assertFalse(mutant.isDetected());
        assertEquals("", mutant.getKillingTest());

        final Mutator mutator = Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("A_SUFFIX", mutant.getMutatorSuffix());

    }

    @Test
    public void testBuild_withKillingTestAndMinimalArguments() throws Exception {

        //@formatter:off
        final Mutant mutant = subject
                               .mutantStatus(MutantStatus.KILLED)
                               .inSourceFile("someSource.java")
                               .inClass("some.package.SomeClass")
                               .inMethod("aMethod")
                               .withMethodParameters("methodDescription")
                               .usingMutator(Mutator.find("ARGUMENT_PROPAGATION"))
                               .killedBy("killingTest")
                               .build();
        // @formatter:on
        assertNotNull(mutant);
        assertEquals(MutantStatus.KILLED, mutant.getMutantStatus());
        assertEquals("someSource.java", mutant.getSourceFile());
        assertEquals("some.package.SomeClass", mutant.getMutatedClass());
        assertEquals("aMethod", mutant.getMutatedMethod());
        assertEquals("methodDescription", mutant.getMethodDescription());
        assertEquals("killingTest", mutant.getKillingTest());
        // implicit default values
        assertEquals(0, mutant.getLineNumber());
        assertEquals(0, mutant.getIndex());
        assertFalse(mutant.isDetected());

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals(mutator, mutant.getMutator());
        assertEquals("", mutant.getMutatorSuffix());

    }

}
