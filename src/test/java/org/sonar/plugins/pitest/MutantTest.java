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

  public static final String INLINE_CONSTANT_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator";
  public static final String RETURN_VALS_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator";

  @Test
  public void should_generate_a_json_string() {
    Mutant m1 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 17, INLINE_CONSTANT_MUTATOR);
    Mutant m2 = new Mutant(false, MutantStatus.SURVIVED, "com.foo.bar.qix", 17, RETURN_VALS_MUTATOR);
    Mutant m3 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 42, INLINE_CONSTANT_MUTATOR);

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

  @Test
  public void should_get_path_to_source_file() {
    // given
    Mutant mutant = new Mutant(true, MutantStatus.KILLED, "com.foo.Bar", 17, INLINE_CONSTANT_MUTATOR);
    // when
    String path = mutant.sourceRelativePath();
    //then
    assertThat(path).isEqualTo("com/foo/Bar.java");
  }

  @Test
  public void should_get_path_to_source_file_for_an_anonymous_inner_class() {
    // given
    Mutant mutant = new Mutant(true, MutantStatus.KILLED, "com.foo.Bar$1", 17, INLINE_CONSTANT_MUTATOR);
    // when
    String path = mutant.sourceRelativePath();
    //then
    assertThat(path).isEqualTo("com/foo/Bar.java");
  }
}
