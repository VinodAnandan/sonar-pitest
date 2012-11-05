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

public enum MutantStatus {
  NO_COVERAGE, KILLED, SURVIVED, MEMORY_ERROR, TIMED_OUT, UNKNOWN;

  public static MutantStatus parse(String statusName) {
    if (statusName == null) {
      return unknown(statusName);
    }
    for (MutantStatus mutantStatus : MutantStatus.values()) {
      if (mutantStatus.name().equals(statusName)) {
        return mutantStatus;
      }
    }
    return unknown(statusName);
  }

  private static MutantStatus unknown(String statusName) {
    return UNKNOWN;
  }

  boolean is(MutantStatus... mutantStatuses) {
    if (mutantStatuses == null) {
      return false;
    }
    for (MutantStatus mutantStatus : mutantStatuses) {
      if (this == mutantStatus) {
        return true;
      }
    }
    return false;
  }
}