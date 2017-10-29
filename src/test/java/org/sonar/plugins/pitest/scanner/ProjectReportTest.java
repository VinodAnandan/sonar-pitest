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

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.sonar.plugins.pitest.domain.Mutant;
import org.sonar.plugins.pitest.domain.TestMutantBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectReportTest {

  @Test
  public void should_organize_by_relative_path() {
    // given
    Mutant m1 = new TestMutantBuilder().className("com.foo.bar.Toto").sourceFile("Toto.java").build();
    Mutant m2 = new TestMutantBuilder().className("com.foo.bar.Toto").sourceFile("Toto.java").build();
    Mutant m3 = new TestMutantBuilder().className("com.foo.bar.differentPackage.Toto").sourceFile("Toto.java").build();
    Mutant m4 = new TestMutantBuilder().className("com.foo.bar.qix.Tata").sourceFile("Tata.java").build();
    Mutant m5 = new TestMutantBuilder().className("bar.Toto").sourceFile("Toto.kt").build();

    // when
    ProjectReport report = new ProjectReport(Arrays.asList(m1, m2, m3, m4, m5));

    // then
    Collection<SourceFileReport> sourceFileReports = report.getSourceFileReports();
    assertThat(sourceFileReports).hasSize(4);
    assertThat(sourceFileReports)
      .usingElementComparatorOnFields("sourceFileRelativePath")
      .containsOnly(
        new SourceFileReport("com/foo/bar/Toto.java"),
        new SourceFileReport("com/foo/bar/differentPackage/Toto.java"),
        new SourceFileReport("com/foo/bar/qix/Tata.java"),
        new SourceFileReport("Toto.kt"));
  }

  @Test
  public void should_collect_in_same_report_when_in_same_file() {
    // given
    Mutant m1 = new TestMutantBuilder().className("com.foo.bar.Toto").sourceFile("Toto.java").build();
    Mutant m2 = new TestMutantBuilder().className("com.foo.bar.Toto").sourceFile("Toto.java").build();

    // when
    ProjectReport report = new ProjectReport(Arrays.asList(m1, m2));

    // then
    Collection<SourceFileReport> sourceFileReports = report.getSourceFileReports();
    assertThat(sourceFileReports).hasSize(1);
    assertThat(sourceFileReports)
      .usingElementComparatorOnFields("sourceFileRelativePath")
      .containsOnly(
        new SourceFileReport("com/foo/bar/Toto.java"));

    SourceFileReport sourceFileReport = sourceFileReports.iterator().next();
    assertThat(sourceFileReport.getMutationsTotal()).isEqualTo(2);

  }
}
