package org.sonar.plugins.pitest.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void testMutantStatusString() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testInSourceFile() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testInClass() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testInMethod() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testWithMethodParameters() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testInLine() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testUsingMutatorMutator() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testUsingMutatorString() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testAtIndex() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testKilledBy() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testBuild() throws Exception {

        throw new RuntimeException("not yet implemented");
    }

}
