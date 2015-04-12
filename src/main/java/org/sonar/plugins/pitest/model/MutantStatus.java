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

/**
 * Enumeration of status a {@link Mutant} may have.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 */
public enum MutantStatus {
  /**
   * The mutant was not covered by a test (Lurker)
   */
  NO_COVERAGE(true),
  /**
   * The mutant was killed by a test
   */
  KILLED(false),
  /**
   * The mutant was covered but not killed by a test (Survivor)
   */
  SURVIVED(true),
  /**
   * The mutant was killed by a memory error during mutation analysis (i.e. memory leak caused by the mutant)
   */
  MEMORY_ERROR(false),
  /**
   * The mutant was killed by a time-out during the mutation analysis (i.e. endless loop caused by the mutant)
   */
  TIMED_OUT(false),
  /**
   * The status of the mutant is unknown.
   */
  UNKNOWN(true);

  private boolean alive;

  MutantStatus(final boolean alive) {

    this.alive = alive;
  }

  /**
   * Indicates whether the status represents an alive or a killed mutant
   *
   * @return <code>true</code> if the {@link Mutant} is still alive.
   */
  public boolean isAlive() {

    return alive;
  }

  /**
   * Parses the String to a MutantStatus.
   *
   * @param statusName
   *         the String representation of the status.
   *
   * @return If the statusName is <code>null</code> or does not represent a valid mutant, UNKNOWN is returned,
   * otherwise the matching status.
   */
  public static MutantStatus parse(final String statusName) {

    if (statusName == null) {
      return UNKNOWN;
    }
    for (final MutantStatus mutantStatus : MutantStatus.values()) {
      if (mutantStatus.name().equals(statusName)) {
        return mutantStatus;
      }
    }
    return UNKNOWN;
  }
}
