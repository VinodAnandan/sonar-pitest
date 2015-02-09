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

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class MutantHelperTest {

    @Test
    public void testToJson_equals() throws JSONException {

        final Mutant m1 = new Mutant(true, MutantStatus.KILLED, "SomeClass.java", "com.foo.bar", "method",
                "methodDesc", 17, Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator"),
                "", 5, "killingTest");
        final Mutant m2 = new Mutant(false, MutantStatus.SURVIVED, "SomeClass.java", "com.foo.bar.qix", "method",
                "methodDesc", 17, Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"), "",
                5,

                "killingTest");
        final Mutant m3 = new Mutant(true, MutantStatus.KILLED, "SomeClass.java", "com.foo.bar", "method",
                "methodDesc", 42, Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator"),
                "", 5, "killingTest");

        final String result = MutantHelper.toJson(Arrays.asList(m1, m2, m3));
        ////@formatter:off
        final String expected =
                "{\"17\":"
                    + "[{\"detected\":true,\"status\":\"KILLED\",\"sourceFile\":\"SomeClass.java\",\"mutatedClass\":\"com.foo.bar\",\"mutatedMethod\":\"method\",\"mutator\":\"INLINE_CONSTS\",\"violationDescription\":\"Alive Mutant: An inline constant has been changed without being detected by a test.\"},"
                    + "{\"detected\":false,\"status\":\"SURVIVED\",\"sourceFile\":\"SomeClass.java\",\"mutatedClass\":\"com.foo.bar.qix\",\"mutatedMethod\":\"method\",\"mutator\":\"RETURN_VALS\",\"violationDescription\":\"Alive Mutant: The return value of a method call has been replaced without being detected by a test.\"}"
                    + "],"
                + "\"42\":"
                    + "[{\"detected\":true,\"status\":\"KILLED\",\"sourceFile\":\"SomeClass.java\",\"mutatedClass\":\"com.foo.bar\",\"mutatedMethod\":\"method\",\"mutator\":\"INLINE_CONSTS\",\"violationDescription\":\"Alive Mutant: An inline constant has been changed without being detected by a test.\"}]}";
        // @formatter:on

        JSONAssert.assertEquals(expected, result, false);
    }

    @Test
    public void testNewMutant() throws Exception {

        final MutantBuilder builder = MutantHelper.newMutant();
        assertNotNull(builder);
    }
}
