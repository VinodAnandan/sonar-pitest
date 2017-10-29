/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2017 Vinod Anandan
 * vinod@owasp.org
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest.scanner;

import org.junit.Test;
import org.sonar.plugins.pitest.domain.Mutant;
import org.sonar.plugins.pitest.domain.MutantStatus;
import org.sonar.plugins.pitest.domain.Mutator;
import org.sonar.plugins.pitest.domain.TestMutantBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class SourceFileReportTest {

  public static final String INLINE_CONSTANT_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator";
  public static final String RETURN_VALS_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator";

  @Test
  public void should_have_correct_relative_path() {
    // given
    Mutant m1 = new TestMutantBuilder().detected(true).mutantStatus(MutantStatus.KILLED).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(17)
      .mutator(Mutator.INLINE_CONS).sourceFile("Qix.java").killingTest("killingtest93").build();

    // when
    SourceFileReport fileMutants = new SourceFileReport("com/foo/bar/Qix.java");
    fileMutants.addMutant(m1);
    
    // then
    assertThat(fileMutants.getRelativePath()).isEqualTo("com/foo/bar/Qix.java");
  }
  
  @Test
  public void should_generate_a_json_string_with_all_data_from_one_line() {
    // given
    Mutant m1 = new TestMutantBuilder().detected(true).mutantStatus(MutantStatus.KILLED).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(17)
      .mutator(Mutator.INLINE_CONS).sourceFile("Qix.java").killingTest("killingtest93").build();
    Mutant m2 = new TestMutantBuilder().detected(false).mutantStatus(MutantStatus.SURVIVED).className("com.foo.bar.Qix").mutatedMethod("anotherMutatedMethod").lineNumber(17)
      .mutator(Mutator.RETURN_VALS).sourceFile("Qix.java").build();

    // when
    SourceFileReport fileMutants = new SourceFileReport("com/foo/bar/Qix.java");
    fileMutants.addMutant(m1);
    fileMutants.addMutant(m2);
    // then
    String result = fileMutants.toJSON();
    assertThat(result)
      .isEqualTo("{\"17\":[" +
        "{ \"d\" : true, \"s\" : \"KILLED\", \"c\" : \"com.foo.bar.Qix\", \"mname\" : \"Inline Constant Mutator\", \"mdesc\" : \"An inline constant has been changed\", \"sourceFile\" : \"Qix.java\", \"mmethod\" : \"mutatedMethod\", \"l\" : \"17\", \"killtest\" : \"killingtest93\" },"
        +
        "{ \"d\" : false, \"s\" : \"SURVIVED\", \"c\" : \"com.foo.bar.Qix\", \"mname\" : \"Return Values Mutator\", \"mdesc\" : \"The return value of a method call has been replaced\", \"sourceFile\" : \"Qix.java\", \"mmethod\" : \"anotherMutatedMethod\", \"l\" : \"17\" }"
        +
        "]}");
  }

  @Test
  public void should_generate_a_json_string_with_one_mutant() {
    // given
    Mutant m1 = new TestMutantBuilder().detected(true).mutantStatus(MutantStatus.KILLED).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(17)
      .mutator(Mutator.INLINE_CONS).sourceFile("Qix.java").killingTest("killingtest93").build();

    // when
    SourceFileReport fileMutants = new SourceFileReport("com/foo/bar/Qix.java");
    fileMutants.addMutant(m1);
    // then
    String result = fileMutants.toJSON();
    assertThat(result)
      .isEqualTo("{\"17\":[" +
        "{ \"d\" : true, \"s\" : \"KILLED\", \"c\" : \"com.foo.bar.Qix\", \"mname\" : \"Inline Constant Mutator\", \"mdesc\" : \"An inline constant has been changed\", \"sourceFile\" : \"Qix.java\", \"mmethod\" : \"mutatedMethod\", \"l\" : \"17\", \"killtest\" : \"killingtest93\" }"
        + "]}");
  }

  @Test
  public void should_generate_an_empty_json_string_when_no_mutant_found() {
    // given
    SourceFileReport fileMutants = new SourceFileReport("FooBar.java");
    // when
    String json = fileMutants.toJSON();
    // then
    assertThat(json).isNullOrEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void fails_if_relative_paths_dont_match() {
    // given
    Mutant m1 = new TestMutantBuilder().mutantStatus(MutantStatus.KILLED).className("com.foo.bar.Foo").mutatedMethod("mutatedMethod").lineNumber(42).mutator(Mutator.EXP_MEMBER_VAR)
      .sourceFile("Foo.kt").build();

    // when
    SourceFileReport fileMutants = new SourceFileReport("com/foo/bar/Qix.java");
    fileMutants.addMutant(m1);
  }

  @Test
  public void should_collect_mutant_metrics() {
    // given

    Mutant m1 = new TestMutantBuilder().mutantStatus(MutantStatus.KILLED).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(17).mutator(Mutator.INLINE_CONS)
      .sourceFile("Qix.java").build();
    Mutant m2 = new TestMutantBuilder().mutantStatus(MutantStatus.SURVIVED).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(17).mutator(Mutator.RETURN_VALS)
      .sourceFile("Qix.java").build();
    Mutant m3 = new TestMutantBuilder().mutantStatus(MutantStatus.KILLED).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(42).mutator(Mutator.EXP_MEMBER_VAR)
      .sourceFile("Qix.java").build();
    Mutant m4 = new TestMutantBuilder().mutantStatus(MutantStatus.NO_COVERAGE).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(42)
      .mutator(Mutator.EXP_MEMBER_VAR).sourceFile("Qix.java").build();
    Mutant m5 = new TestMutantBuilder().mutantStatus(MutantStatus.UNKNOWN).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(42)
      .mutator(Mutator.EXP_MEMBER_VAR).sourceFile("Qix.java").build();
    Mutant m6 = new TestMutantBuilder().mutantStatus(MutantStatus.OTHER).className("com.foo.bar.Qix").mutatedMethod("mutatedMethod").lineNumber(42).mutator(Mutator.EXP_MEMBER_VAR)
      .sourceFile("Qix.java").build();

    SourceFileReport sourceFileReport = new SourceFileReport("com/foo/bar/Qix.java");

    // when
    sourceFileReport.addMutant(m1);
    sourceFileReport.addMutant(m2);
    sourceFileReport.addMutant(m3);
    sourceFileReport.addMutant(m4);
    sourceFileReport.addMutant(m5);
    sourceFileReport.addMutant(m6);

    // then
    assertThat(sourceFileReport.getMutationsTotal()).isEqualTo(6);
    assertThat(sourceFileReport.getMutants()).hasSize(6);
    assertThat(sourceFileReport.getMutationsKilled()).isEqualTo(2);
    assertThat(sourceFileReport.getMutationsSurvived()).isEqualTo(1);
    assertThat(sourceFileReport.getMutationsNoCoverage()).isEqualTo(1);
    assertThat(sourceFileReport.getMutationsUnknown()).isEqualTo(1);
    assertThat(sourceFileReport.getMutationsOther()).isEqualTo(1);
    /*
     * NO_COVERAGE("NO_COVERAGE"),
     * KILLED("KILLED"),
     * SURVIVED("SURVIVED"),
     * OTHER("TIMED_OUT", "NON_VIABLE", "MEMORY_ERROR", "RUN_ERROR"),
     * UNKNOWN;
     */
  }
}
