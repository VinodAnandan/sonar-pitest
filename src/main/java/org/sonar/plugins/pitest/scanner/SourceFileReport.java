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
package org.sonar.plugins.pitest.scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.sonar.plugins.pitest.domain.Mutant;

/**
 * Mutants for a given java source file
 */
public class SourceFileReport {
  private final String sourceFileRelativePath;
  private final List<Mutant> mutants = new ArrayList<>();
  private int mutationsNoCoverage = 0;
  private int mutationsKilled = 0;
  private int mutationsSurvived = 0;
  private int mutationsOther = 0;
  private int mutationsUnknown = 0;

  public SourceFileReport(String sourceFileRelativePath) {
    this.sourceFileRelativePath = sourceFileRelativePath;
  }

  public String toJSON() {
    if (mutants.isEmpty()) {
      return null;
    }
    Map<Integer, List<String>> mutantsByLine = new HashMap<>();

    for (Mutant mutant : mutants) {
      if (!mutantsByLine.containsKey(mutant.lineNumber())) {
        mutantsByLine.put(mutant.lineNumber(), new ArrayList<String>());
      }
      mutantsByLine.get(mutant.lineNumber()).add(mutant.toString());
    }

    StringBuilder builder = new StringBuilder();
    builder.append("{");
    boolean first = true;
    for (Entry<Integer, List<String>> entry : mutantsByLine.entrySet()) {
      if (!first) {
        builder.append(",");
      }
      first = false;
      builder.append("\"").append(entry.getKey()).append("\":");
      builder.append("[");
      for (String mutant : entry.getValue()) {
        builder.append(mutant).append(',');
      }
      builder.deleteCharAt(builder.length() - 1); // remove last ','
      builder.append("]");
    }
    builder.append("}");

    return builder.toString();
  }

  public void addMutant(Mutant mutant) {
    if (!sourceFileRelativePath.equals(mutant.sourceRelativePath())) {
      throw new IllegalArgumentException("Relative paths do not match: "
        + sourceFileRelativePath
        + " vs "
        + mutant.sourceRelativePath());
    }
    mutants.add(mutant);
    switch (mutant.mutantStatus) {
      case NO_COVERAGE:
        mutationsNoCoverage++;
        break;
      case KILLED:
        mutationsKilled++;
        break;
      case SURVIVED: // Only survived mutations are saved as violations
        mutationsSurvived++;
        break;
      case OTHER:
        mutationsOther++;
        break;
      case UNKNOWN:
        mutationsUnknown++;
        break;
    }
  }

  public String getRelativePath() {
    return sourceFileRelativePath;
  }

  public Collection<Mutant> getMutants() {
    return Collections.unmodifiableList(mutants);
  }

  public int getMutationsTotal() {
    return mutants.size();
  }

  int getMutationsNoCoverage() {
    return mutationsNoCoverage;
  }

  int getMutationsKilled() {
    return mutationsKilled;
  }

  int getMutationsSurvived() {
    return mutationsSurvived;
  }

  int getMutationsOther() {
    return mutationsOther;
  }

  int getMutationsUnknown() {
    return mutationsUnknown;
  }

  @Override
  public String toString() {
    return "SourceFileReport [sourceFileRelativePath=" + sourceFileRelativePath + ", mutants=" + mutants + ", mutationsNoCoverage=" + mutationsNoCoverage + ", mutationsKilled="
      + mutationsKilled + ", mutationsSurvived=" + mutationsSurvived + ", mutationsOther=" + mutationsOther + ", mutationsUnknown=" + mutationsUnknown + "]";
  }

}
