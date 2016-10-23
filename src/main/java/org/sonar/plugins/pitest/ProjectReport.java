/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 Alexandre Victoor
 * alexvictoor@gmail.com
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


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProjectReport {

  private final Map<String, SourceFileReport> sourceFileReports = new HashMap<String, SourceFileReport>();

  private ProjectReport() {
  }

  private void build(Collection<Mutant> mutants) {
    for (Mutant mutant : mutants) {
      String relativePath = mutant.sourceRelativePath();
      final SourceFileReport sourceFileReport;
      if (sourceFileReports.containsKey(relativePath)) {
        sourceFileReport = sourceFileReports.get(relativePath);
      } else {
        sourceFileReport = new SourceFileReport(relativePath);
        sourceFileReports.put(relativePath, sourceFileReport);
      }
      sourceFileReport.addMutant(mutant);
    }
  }

  public Collection<SourceFileReport> getSourceFileReports() {
    return sourceFileReports.values();
  }

  public static ProjectReport buildFromMutants(Collection<Mutant> mutants) {
    ProjectReport projectReport = new ProjectReport();
    projectReport.build(mutants);
    return projectReport;
  }

}
