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
