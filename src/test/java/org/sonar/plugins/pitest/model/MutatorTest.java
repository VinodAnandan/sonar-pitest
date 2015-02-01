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
