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
package org.sonar.plugins.pitest;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.sonar.test.TestUtils;

public class ReportFinderTest {

    @Test
    public void should_find_report_file() {

        // given
        final ReportFinder finder = new ReportFinder();
        final File xmlFile = TestUtils.getResource("mutations.xml");
        final File directory = xmlFile.getParentFile();

        // when
        final File report = finder.findReport(directory);

        // then
        assertThat(report).isEqualTo(xmlFile);
    }

    @Test
    public void should_return_null_if_no_report() {

        // given
        final ReportFinder finder = new ReportFinder();
        final File directory = TestUtils.getResource("fake_libs");

        // when
        final File report = finder.findReport(directory);

        // then
        assertThat(report).isNull();
    }

    @Test
    public void should_return_null_if_directory_does_not_exist() {

        // given
        final ReportFinder finder = new ReportFinder();
        final File directory = TestUtils.getResource("imaginary");

        // when
        final File report = finder.findReport(directory);

        // then
        assertThat(report).isNull();
    }
}
