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
package org.sonar.plugins.pitest;

import static org.fest.assertions.Assertions.assertThat;
import org.junit.Test;

import com.google.common.collect.Lists;


public class MutantTest {

  @Test
  public void should_generate_a_json_string() {
    Mutant m1 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 17, "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator");
    Mutant m2 = new Mutant(false, MutantStatus.SURVIVED, "com.foo.bar.qix", 17, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator");
    Mutant m3 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 42, "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator");

    String result = Mutant.toJSON(Lists.newArrayList(m1, m2, m3));
    assertThat(result)
      .isEqualTo("{\"17\":[" +
      		"{ \"d\" : true, \"s\" : \"KILLED\", \"c\" : \"com.foo.bar\", \"mname\" : \"Inline Constant Mutator\", \"mdesc\" : \"An inline constant has been changed\"  }," +
      		"{ \"d\" : false, \"s\" : \"SURVIVED\", \"c\" : \"com.foo.bar.qix\", \"mname\" : \"Return Values Mutator\", \"mdesc\" : \"The return value of a method call has been replaced\"  }" +
      		"]," +
      		"\"42\":[" +
      		"{ \"d\" : true, \"s\" : \"KILLED\", \"c\" : \"com.foo.bar\", \"mname\" : \"Inline Constant Mutator\", \"mdesc\" : \"An inline constant has been changed\"  }" +
      		"]}");
  }
}
