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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SourceFileMutantsTest {

  public static final String INLINE_CONSTANT_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator";
  public static final String RETURN_VALS_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator";

  @Test
  public void should_generate_a_json_string_with_all_data() {
    // given
    Mutant m1 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 17, INLINE_CONSTANT_MUTATOR);
    Mutant m2 = new Mutant(false, MutantStatus.SURVIVED, "com.foo.bar.qix", 17, RETURN_VALS_MUTATOR);
    Mutant m3 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 42, INLINE_CONSTANT_MUTATOR);
    // when
    SourceFileMutants fileMutants = new SourceFileMutants();
    fileMutants.addMutant(m1);
    fileMutants.addMutant(m2);
    fileMutants.addMutant(m3);
    // then
    String result = fileMutants.toJSON();
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
  public void should_generate_an_empty_json_string_when_no_mutant_found() {
    // given
    SourceFileMutants fileMutants = new SourceFileMutants();
    // when
    String json = fileMutants.toJSON();
    // then
    assertThat(json).isNullOrEmpty();
  }
    @Test
  public void should_collect_mutant_metrics() {
    // given
    Mutant m1 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 17, "key1");
    Mutant m2 = new Mutant(false, MutantStatus.SURVIVED, "com.foo.bar.qix", 17, "key2");
    Mutant m3 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar", 15, "key3");
    SourceFileMutants sourceFileMutants = new SourceFileMutants();

    // when
    sourceFileMutants.addMutant(m1);
    sourceFileMutants.addMutant(m2);
    sourceFileMutants.addMutant(m3);

    // then
    assertThat(sourceFileMutants.mutants).hasSize(3);
    assertThat(sourceFileMutants.getMutationsTotal()).isEqualTo(3);
    assertThat(sourceFileMutants.getMutationsDetected()).isEqualTo(2);
    assertThat(sourceFileMutants.getMutationsKilled()).isEqualTo(2);
    assertThat(sourceFileMutants.getMutationsMemoryError()).isEqualTo(0);
    assertThat(sourceFileMutants.getMutationsNoCoverage()).isEqualTo(0);
    assertThat(sourceFileMutants.getMutationsTimedOut()).isEqualTo(0);
    assertThat(sourceFileMutants.getMutationsUnknown()).isEqualTo(0);

  }
}