/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009 Alexandre Victoor
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

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class MutantHelperTest {

    @Test
    public void should_generate_a_json_string() {

        final Mutant m1 = new Mutant(true, MutantStatus.KILLED, "SomeClass.java", "com.foo.bar", "method",
                "methodDesc", 17, Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator"),
                5, "killingTest");
        final Mutant m2 = new Mutant(false, MutantStatus.SURVIVED, "SomeClass.java", "com.foo.bar.qix", "method",
                "methodDesc", 17, Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"), 5,
                "killingTest");
        final Mutant m3 = new Mutant(true, MutantStatus.KILLED, "SomeClass.java", "com.foo.bar", "method",
                "methodDesc", 42, Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator"),
                5, "killingTest");

        final String result = MutantHelper.toJson(Arrays.asList(m1, m2, m3));
        System.out.println(result);
        assertThat(result)
                .isEqualTo(
                        "{\"17\":["
                                + "{ \"d\" : true, \"s\" : \"KILLED\", \"c\" : \"com.foo.bar\", \"mname\" : \"Inline Constant Mutator\", \"mdesc\" : \"An inline constant has been changed\"  },"
                                + "{ \"d\" : false, \"s\" : \"SURVIVED\", \"c\" : \"com.foo.bar.qix\", \"mname\" : \"Return Values Mutator\", \"mdesc\" : \"The return value of a method call has been replaced\"  }"
                                + "],"
                                + "\"42\":["
                                + "{ \"d\" : true, \"s\" : \"KILLED\", \"c\" : \"com.foo.bar\", \"mname\" : \"Inline Constant Mutator\", \"mdesc\" : \"An inline constant has been changed\"  }"
                                + "]}");
    }
}
