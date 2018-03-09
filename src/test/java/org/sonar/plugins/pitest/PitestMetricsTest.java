/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2018 Vinod Anandan
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

import java.io.Serializable;
import java.util.List;
import org.junit.Test;
import org.sonar.api.measures.Metric;

import static org.assertj.core.api.Assertions.assertThat;

public class PitestMetricsTest {

  @Test
  public void mutationsNotCoveredMetricIsCreatedCorrectly() {
    // given

    // when
    Metric<Serializable> mutationsNotCovered = PitestMetrics.MUTATIONS_NOT_COVERED;

    // then
    assertThat(mutationsNotCovered.getDescription()).contains("not covered");
    assertThat(mutationsNotCovered.getDirection()).isEqualTo(Metric.DIRECTION_WORST);
    assertThat(mutationsNotCovered.getQualitative()).isFalse();
    assertThat(mutationsNotCovered.getDomain()).isEqualTo(PitestMetrics.PITEST_DOMAIN);
  }

  @Test
  public void verifyMetricsSize() {
    // given

    // when
    List<Metric> metrics = new PitestMetrics().getMetrics();

    // then
    assertThat(metrics).hasSize(8);
  }

}
