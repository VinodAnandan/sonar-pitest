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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MutatorTest {

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

}
