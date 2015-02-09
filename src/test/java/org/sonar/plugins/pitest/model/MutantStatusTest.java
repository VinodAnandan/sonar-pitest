package org.sonar.plugins.pitest.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MutantStatusTest {

    @Test
    public void testIsAlive() throws Exception {

        assertTrue(MutantStatus.NO_COVERAGE.isAlive());
        assertTrue(MutantStatus.UNKNOWN.isAlive());
        assertTrue(MutantStatus.SURVIVED.isAlive());
        assertFalse(MutantStatus.MEMORY_ERROR.isAlive());
        assertFalse(MutantStatus.TIMED_OUT.isAlive());
        assertFalse(MutantStatus.KILLED.isAlive());
    }

    @Test
    public void testParse() throws Exception {

        assertEquals(MutantStatus.NO_COVERAGE, MutantStatus.parse("NO_COVERAGE"));
        assertEquals(MutantStatus.KILLED, MutantStatus.parse("KILLED"));
        assertEquals(MutantStatus.SURVIVED, MutantStatus.parse("SURVIVED"));
        assertEquals(MutantStatus.MEMORY_ERROR, MutantStatus.parse("MEMORY_ERROR"));
        assertEquals(MutantStatus.TIMED_OUT, MutantStatus.parse("TIMED_OUT"));
        assertEquals(MutantStatus.UNKNOWN, MutantStatus.parse("UNKNOWN"));
    }

}
