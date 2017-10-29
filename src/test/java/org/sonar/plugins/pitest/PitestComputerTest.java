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
package org.sonar.plugins.pitest;

import org.junit.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerDefinition;
import org.sonar.api.ce.measure.Settings;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinitionContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PitestComputerTest {

  @Test
  public void definition_should_have_no_input_metrics_eight_output_metrics() {
    // given
    TestMeasureComputerDefinitionContext context = new TestMeasureComputerDefinitionContext();
    PitestComputer computer = new PitestComputer();

    // when
    MeasureComputerDefinition def = computer.define(context);

    // then
    assertThat(def).isNotNull();
    assertThat(def.getInputMetrics()).isEmpty();
    assertThat(def.getOutputMetrics()).containsOnly("pitest_mutations_noCoverage", "pitest_mutations_total", "pitest_mutations_killed", "pitest_mutations_survived",
      "pitest_mutations_error", "pitest_mutations_unknown", "pitest_mutations_data", "pitest_mutations_killed_percent");

  }

  @Test
  public void top_level_measures_are_preserved() {
    // given
    PitestComputer sut = new PitestComputer();
    MeasureComputerDefinition measureComputerDefinition = sut.define(new TestMeasureComputerDefinitionContext());
    TestMeasureComputerContext context = new TestMeasureComputerContext(null, null, measureComputerDefinition);
    context.addMeasure("pitest_mutations_killed", 3);

    // when
    sut.compute(context);

    // then
    assertThat(context.getMeasure("pitest_mutations_killed").getIntValue()).isEqualTo(3);

  }

  @Test
  public void children_measures_are_calculated() {
    // given
    PitestComputer sut = new PitestComputer();
    MeasureComputerDefinition measureComputerDefinition = sut.define(new TestMeasureComputerDefinitionContext());
    TestMeasureComputerContext context = new TestMeasureComputerContext(null, null, measureComputerDefinition);
    context.addChildrenMeasures("pitest_mutations_killed", 3, 5, 7, 9);

    // when
    sut.compute(context);

    // then
    assertThat(context.getMeasure("pitest_mutations_killed").getIntValue()).isEqualTo(24);

  }

  @Test
  public void calculateCoveragePercent() {
    // given
    PitestComputer coverageComputer = new PitestComputer();
    TestMeasureComputerDefinitionContext defContext = new TestMeasureComputerDefinitionContext();
    TestMeasureComputerContext context = new TestMeasureComputerContext(mock(Component.class), mock(Settings.class), coverageComputer.define(defContext));
    context.addMeasure(PitestMetrics.MUTATIONS_GENERATED_KEY, 10);
    context.addMeasure(PitestMetrics.MUTATIONS_KILLED_KEY, 2);

    // when
    coverageComputer.compute(context);

    // then
    assertThat(context.getMeasure(PitestMetrics.MUTATIONS_KILLED_PERCENT_KEY).getDoubleValue()).isEqualTo(20);

  }
}
