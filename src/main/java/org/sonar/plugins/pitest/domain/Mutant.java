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

import javax.annotation.Nullable;

/**
 * Mutation information from the pitest report.
 *
 */
public final class Mutant {

  public final boolean detected;
  public final MutantStatus mutantStatus;
  public final MutantLocation mutantLocation;
  public final Mutator mutator;
  public final int index;
  public final String description;
  public final String killingTest;

  public Mutant(boolean detected, MutantStatus mutantStatus, MutantLocation mutantLocation, String mutatorKey, int index, String description, @Nullable String killingTest) {
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.mutantLocation = mutantLocation;
    this.mutator = Mutator.parse(mutatorKey);
    this.index = index;
    this.description = description;
    this.killingTest = killingTest;
  }

  public String sourceRelativePath() {
    return mutantLocation.getRelativePath();
  }

  public String violationDescription() {
    StringBuilder builder = new StringBuilder(mutator.getDescription());
    builder.append(" without breaking the tests");
    builder.append(" [").append(description).append("]");
    return builder.toString();
  }

  public int lineNumber() {
    return mutantLocation.lineNumber;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder()
      .append("{ \"d\" : ").append(detected)
      .append(", \"s\" : \"").append(mutantStatus).append("\"")
      .append(", \"c\" : \"").append(mutantLocation.getClassName()).append("\"")
      .append(", \"mname\" : \"").append(mutator.getName()).append("\"")
      .append(", \"mdesc\" : \"").append(mutator.getDescription()).append("\"")
      .append(", \"sourceFile\" : \"").append(mutantLocation.getSourceFile()).append("\"")
      .append(", \"mmethod\" : \"").append(mutantLocation.getMutatedMethod()).append("\"")
      .append(", \"l\" : \"").append(mutantLocation.getLineNumber()).append("\"");

    if (killingTest != null) {
      builder.append(", \"killtest\" : \"").append(killingTest).append("\"");
    }

    builder.append(" }");
    return builder.toString();
  }

}
