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
package org.sonar.plugins.pitest.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutantStatusTest {
  /*
   * creation from PitestDetectionStatus
   */
  @Test
  public void other_is_returned_for_timed_out() {
    MutantStatus status = MutantStatus.fromPitestDetectionStatus("TIMED_OUT");
    assertThat(status).isEqualTo(MutantStatus.OTHER);
  }

  @Test
  public void other_is_returned_for_memory_error() {
    MutantStatus status = MutantStatus.fromPitestDetectionStatus("MEMORY_ERROR");
    assertThat(status).isEqualTo(MutantStatus.OTHER);
  }

  @Test
  public void killed_is_created_correctly() {
    MutantStatus status = MutantStatus.fromPitestDetectionStatus("KILLED");
    assertThat(status).isEqualTo(MutantStatus.KILLED);
  }

  /*
   * parse
   */
  @Test
  public void parse_upper_case_KILLED_succeeds() {
    MutantStatus status = MutantStatus.parse("KILLED");
    assertThat(status).isEqualTo(MutantStatus.KILLED);
  }

  @Test
  public void parse_lower_case_killed_returns_unknown() {
    MutantStatus status = MutantStatus.parse("killed");
    assertThat(status).isEqualTo(MutantStatus.UNKNOWN);
  }

  @Test
  public void parse_empty_string_returns_unknown() {
    MutantStatus status = MutantStatus.parse("");
    assertThat(status).isEqualTo(MutantStatus.UNKNOWN);
  }

  @Test
  public void parse_null_returns_unknown() {
    MutantStatus status = MutantStatus.parse(null);
    assertThat(status).isEqualTo(MutantStatus.UNKNOWN);
  }

}
