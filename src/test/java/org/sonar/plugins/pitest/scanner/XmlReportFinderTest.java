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

import com.google.common.io.Resources;
import java.io.File;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlReportFinderTest {

  @Test
  public void should_find_latest_report_file_with_one_timestamped_folder() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File reportDirectory = new File(Resources.getResource("test-pit-reports-1").getFile());

    // when
    File report = finder.findReport(reportDirectory);

    // then
    assertThat(report).isNotNull();
    report.getAbsolutePath().endsWith("123/mutations.xml");
  }

  @Test
  public void should_find_latest_report_file_with_two_timestamped_folders() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File reportDirectory = new File(Resources.getResource("test-pit-reports-2").getFile());
    new File(Resources.getResource("test-pit-reports-2/123/mutations.xml").getFile()).setLastModified(400);
    new File(Resources.getResource("test-pit-reports-2/124/mutations.xml").getFile()).setLastModified(500);

    // when
    File report = finder.findReport(reportDirectory);

    // then
    assertThat(report).isNotNull();
    report.getAbsolutePath().endsWith("124/mutations.xml");
  }

  @Test
  public void should_find_latest_report_file_with_inconsistent_timestamp() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File reportDirectory = new File(Resources.getResource("test-pit-reports-2").getFile());
    new File(Resources.getResource("test-pit-reports-2/123/mutations.xml").getFile()).setLastModified(600);
    new File(Resources.getResource("test-pit-reports-2/124/mutations.xml").getFile()).setLastModified(500);

    // when
    File report = finder.findReport(reportDirectory);

    // then
    assertThat(report).isNotNull();
    report.getAbsolutePath().endsWith("123/mutations.xml");
  }

  @Test
  public void should_return_null_if_no_report() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File directory = new File(Resources.getResource("fake_libs").getFile());

    // when
    File report = finder.findReport(directory);

    // then
    assertThat(report).isNull();
  }

  @Test
  public void should_return_null_if_input_is_not_a_directory() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File directory = new File(Resources.getResource("Maze.kt").getFile());

    // when
    File report = finder.findReport(directory);

    // then
    assertThat(report).isNull();
  }

  @Test
  public void should_return_null_if_directory_does_not_exist() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File directory = new File("imaginary");

    // when
    File report = finder.findReport(directory);

    // then
    assertThat(report).isNull();
  }
}
