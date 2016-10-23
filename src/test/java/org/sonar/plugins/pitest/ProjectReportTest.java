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


import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectReportTest {

  @Test
  public void should_collect_mutant_metrics() {
    // given
    Mutant m1 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar.Toto", 17, "key1");
    Mutant m2 = new Mutant(false, MutantStatus.SURVIVED, "com.foo.bar.qix.Tata", 17, "key2");
    Mutant m3 = new Mutant(true, MutantStatus.KILLED, "com.foo.bar.Toto", 15, "key3");

    // when
    ProjectReport report
      = ProjectReport.buildFromMutants(
          Arrays.asList(m1, m2, m3)
    );

    // then
    Collection<SourceFileReport> sourceFileReports = report.getSourceFileReports();
    assertThat(sourceFileReports).hasSize(2);
    assertThat(sourceFileReports)
      .usingElementComparatorOnFields("sourceFileRelativePath")
      .containsOnly(
        new SourceFileReport("com/foo/bar/Toto.java"),
        new SourceFileReport("com/foo/bar/qix/Tata.java")
      );

  }
}
