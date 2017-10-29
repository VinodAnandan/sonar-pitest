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
package org.sonar.plugins.pitest.domain;

import java.util.Arrays;
import java.util.List;

/*
 * Note: this is an incomplete list of DetectionStatus values.
 * The complete list is here: https://github.com/hcoles/pitest/blob/master/pitest/src/main/java/org/pitest/mutationtest/DetectionStatus.java
 * 
 * OTHER is used for TIMED_OUT, NON_VIABLE, MEMORY_ERROR, RUN_ERROR, as these have less to say about Test Quality, more indicative of problems with the test fixture
 */
public enum MutantStatus {
  NO_COVERAGE("NO_COVERAGE"),
  KILLED("KILLED"),
  SURVIVED("SURVIVED"),
  OTHER("TIMED_OUT", "NON_VIABLE", "MEMORY_ERROR", "RUN_ERROR"),
  UNKNOWN;

  private final List<String> pitestDetectionStatus;

  MutantStatus(final String... pitestDetectionStatus) {
    this.pitestDetectionStatus = Arrays.asList(pitestDetectionStatus);

  }

  public static MutantStatus fromPitestDetectionStatus(String pitestDetectionStatus) {
    for (MutantStatus mutantStatus : MutantStatus.values()) {
      if (mutantStatus.pitestDetectionStatus.contains(pitestDetectionStatus)) {
        return mutantStatus;
      }
    }
    return UNKNOWN;
  }

  public static MutantStatus parse(String statusName) {
    for (MutantStatus mutantStatus : MutantStatus.values()) {
      if (mutantStatus.name().equals(statusName)) {
        return mutantStatus;
      }
    }
    return UNKNOWN;
  }
}
