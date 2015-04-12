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
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.junit.Test;

public class MutatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testMutator_nullId_exception() throws Exception {

        new Mutator(null, "", "", "", new URL("file:///"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testMutator_nullName_exception() throws Exception {

        new Mutator("", null, "", "", new URL("file:///"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testMutator_nullViolationDescription_exception() throws Exception {

        new Mutator("", "", "", null, new URL("file:///"));

    }

    @Test
    public void testMutator_nullArgument() throws Exception {

        final Mutator mutator = new Mutator("id", "name", null, "violationDescription", null);
        assertEquals("id", mutator.getId());
        assertEquals("name", mutator.getName());
        assertEquals("violationDescription", mutator.getViolationDescription());
        assertNull(mutator.getClassName());
        assertNull(mutator.getMutatorDescriptionLocation());
        assertNotNull(mutator.getMutatorDescription());
        assertEquals("", mutator.getMutatorDescription());
        assertNotNull(mutator.getMutatorDescriptionAsStream());

    }

    @Test
    public void testFind_knownMutator_byID() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotNull(mutator);
        assertEquals("ARGUMENT_PROPAGATION", mutator.getId());
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator",
                mutator.getClassName());
        assertNotNull(mutator.getViolationDescription());
    }

    @Test
    public void testFind_knownMutator_byClassName() throws Exception {

        final Mutator mutator = Mutator
                .find("org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator");
        assertNotNull(mutator);
        assertEquals("ARGUMENT_PROPAGATION", mutator.getId());
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator",
                mutator.getClassName());
        assertNotNull(mutator.getViolationDescription());
    }

    @Test
    public void testFind_knownMutator_byClassNameWithSuffix() throws Exception {

        final Mutator mutator = Mutator
                .find("org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator_WITH_SUFFIX");
        assertNotNull(mutator);
        assertEquals("ARGUMENT_PROPAGATION", mutator.getId());
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator",
                mutator.getClassName());
        assertNotNull(mutator.getViolationDescription());
    }

    @Test
    public void testGetAllMutators() throws Exception {

        // act
        final Collection<Mutator> mutators = Mutator.getAllMutators();

        // assert
        assertNotNull(mutators);
        assertFalse(mutators.isEmpty());
        assertEquals(17, mutators.size());

    }

    @Test
    public void testGetId() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        assertEquals("ARGUMENT_PROPAGATION", mutator.getId());
    }

    @Test
    public void testGetMutatorDescriptionLocation() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        final URL descriptorLocation = mutator.getMutatorDescriptionLocation();
        assertNotNull(descriptorLocation);
    }

    @Test
    public void testGetMutatorDescriptionAsStream() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        final InputStream descStream = mutator.getMutatorDescriptionAsStream();
        assertNotNull(descStream);
    }

    @Test
    public void testGetMutatorDescription() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        final String desc = mutator.getMutatorDescription();
        assertNotNull(desc);
    }

    @Test
    public void testGetViolationDescription() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        final String violationDesc = mutator.getViolationDescription();
        assertNotNull(violationDesc);
    }

    @Test
    public void testGetName() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        final String name = mutator.getName();
        assertEquals("Argument Propagation Mutator", name);
    }

    @Test
    public void testGetClassName() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        final String className = mutator.getClassName();
        assertEquals("org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator", className);
    }

    @Test
    public void testEquals_null_false() throws Exception {

        final Mutator argumentPropagation = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotEquals(argumentPropagation, null);
    }

    @Test
    public void testEquals_different_false() throws Exception {

        final Mutator argumentPropagation = Mutator.find("ARGUMENT_PROPAGATION");
        final Mutator conditionalsBoundary = Mutator.find("CONDITIONALS_BOUNDARY");
        assertNotEquals(argumentPropagation, conditionalsBoundary);
    }

    @Test
    public void testEquals_differentClass_false() throws Exception {

        final Mutator argumentPropagation = Mutator.find("ARGUMENT_PROPAGATION");
        assertNotEquals(argumentPropagation, new Object());
    }

    @Test
    public void testEquals_same_true() throws Exception {

        final Mutator argumentPropagation = Mutator.find("ARGUMENT_PROPAGATION");
        assertEquals(argumentPropagation, argumentPropagation);
    }

    @Test
    public void testEquals_equalsId_true() throws Exception {

        final Mutator argumentPropagation = Mutator.find("ARGUMENT_PROPAGATION");
        final Mutator other = new Mutator("ARGUMENT_PROPAGATION", "someName", "someClass", "someDescription", new URL(
                "file:///"));
        assertEquals(argumentPropagation, other);
    }

    @Test
    public void testHashCode_reproducible() throws Exception {

        final Mutator mutator = Mutator.find("ARGUMENT_PROPAGATION");
        final int expectedHashCode = 31 + mutator.getId().hashCode();

        assertEquals(expectedHashCode, mutator.hashCode());

    }

    @Test
    public void testHashCode_sameMutator() throws Exception {

        final Mutator argumentPropagation = Mutator.find("ARGUMENT_PROPAGATION");
        assertEquals(argumentPropagation.hashCode(), argumentPropagation.hashCode());
    }

    @Test
    public void testHashCode_otherMutatorObject() throws Exception {

        final Mutator argumentPropagation = Mutator.find("ARGUMENT_PROPAGATION");
        final Mutator conditionalsBoundary = Mutator.find("CONDITIONALS_BOUNDARY");

        assertNotEquals(argumentPropagation.hashCode(), conditionalsBoundary.hashCode());
    }

}
